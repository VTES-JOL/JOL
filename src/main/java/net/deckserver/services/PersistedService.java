package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for services that require scheduled persistence, graceful shutdown,
 * and test mode support.
 * <p>
 * This class handles the lifecycle management (scheduling, shutdown, test mode)
 * while allowing subclasses to define their own persistence strategy.
 * <p>
 * NOTE: Shutdown should be triggered via ServletContextListener, not JVM shutdown hooks,
 * to avoid classloader issues in servlet containers.
 * <p>
 * NOTE: The in-memory state held by these services is authoritative and the database is
 * a write-through copy. This assumes a single app node — a second node would diverge
 * immediately, so scaling out requires moving reads to the database first.
 */
public abstract class PersistedService {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    /**
     * Whether ENABLE_TEST_MODE is set. Computed once and shared everywhere, so that
     * static-context call sites (services exposing only static methods) have a single
     * canonical way to check the flag instead of each re-implementing the env lookup.
     */
    private static final boolean TEST_MODE = System.getenv().getOrDefault("ENABLE_TEST_MODE", "false").equals("true");

    public static boolean isTestMode() {
        return TEST_MODE;
    }

    /**
     * Resolves the CDI-managed singleton for a PersistedService subclass —
     * shared by every subclass's private {@code instance()} accessor.
     * <p>
     * Under Quarkus (`quarkus:dev`, or once the app is deployed for real),
     * Arc's container is running and this is a cheap lookup of the
     * `@Startup`-eagerly-created `@Singleton` bean (not `@ApplicationScoped`
     * — that's a CDI "normal scope", meaning `Instance.get()` returns a
     * client proxy whose direct field access silently misses the real
     * instance's state; see {@link ChatService}'s `instance()` javadoc for
     * how this was actually discovered). Plain JUnit tests
     * never bootstrap Quarkus's runtime at all (no `@QuarkusTest` — that's
     * a deliberate choice, not an oversight: see JolServiceExtension, which
     * keeps this test suite fast by avoiding a full Quarkus boot per test
     * class), so `Arc.container()` returns null there — in that case this
     * falls back to a plain lazily-created singleton, cached per type,
     * exactly matching what the old `private static final X INSTANCE = new X()`
     * field used to provide.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends PersistedService> T resolve(Class<T> type, Supplier<T> fallbackFactory) {
        if (Arc.container() != null) {
            return Arc.container().instance(type).get();
        }
        // No CDI container means no automatic @PostConstruct either — call
        // initialize() explicitly so the fallback path still loads data on
        // first use, exactly like the old constructor-calls-load() pattern.
        return (T) FALLBACK_INSTANCES.computeIfAbsent(type, t -> {
            T created = fallbackFactory.get();
            created.initialize();
            return created;
        });
    }

    private static final Map<Class<?>, PersistedService> FALLBACK_INSTANCES = new ConcurrentHashMap<>();

    protected final Logger logger;
    protected final String serviceName;
    protected final boolean testModeEnabled;
    protected final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    protected final ScheduledExecutorService scheduler;

    /**
     * Constructor that initialises the service with scheduled persistence.
     *
     * @param serviceName Name of the service (used for logging and thread naming)
     * @param persistenceIntervalMinutes How often to persist data (in minutes);
     *                                   0 for write-through services that need no background flush
     */
    protected PersistedService(String serviceName, int persistenceIntervalMinutes) {
        this.serviceName = serviceName;
        this.logger = LoggerFactory.getLogger(getClass());
        this.testModeEnabled = TEST_MODE;

        // Create scheduler with daemon thread
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, serviceName + "-Persistence-Scheduler");
            thread.setDaemon(true);
            return thread;
        });

        // Start a scheduled persistence task if not in test mode
        if (!testModeEnabled && persistenceIntervalMinutes > 0) {
            scheduler.scheduleAtFixedRate(
                    this::scheduledPersist,
                    persistenceIntervalMinutes,
                    persistenceIntervalMinutes,
                    TimeUnit.MINUTES
            );
            logger.info("{} scheduled persistence task started (every {} minutes)",
                    serviceName, persistenceIntervalMinutes);
        }

        // DO NOT add shutdown hook here - use ServletContextListener instead
    }

    /**
     * Runs after CDI construction/injection completes — the single place
     * {@link #load()} is invoked from now (subclass constructors used to
     * call it directly; centralized here since it's identical boilerplate
     * in every subclass, and CDI's own lifecycle guarantees this runs before
     * any other bean can observe a fully-constructed instance).
     */
    @PostConstruct
    protected void initialize() {
        load();
    }

    /**
     * CDI-managed graceful shutdown, replacing the explicit
     * JolApplicationInitializer.contextDestroyed() call to {@link #shutdown()}
     * (still present for now, harmless double-call-guarded by
     * isShuttingDown — see {@link #shutdown()} — until that class is retired).
     * <p>
     * Deliberately {@code @Observes ShutdownEvent}, not {@code @PreDestroy}:
     * confirmed empirically that {@code @PreDestroy} races against Arc's own
     * context teardown — Quarkus's Hibernate ORM extension had already closed
     * its EntityManagerFactory by the time some services' {@code @PreDestroy}
     * fired, so their final {@link #shutdown()}-triggered persist() threw
     * "EntityManagerFactory is closed". ShutdownEvent is explicitly documented
     * as firing before Quarkus tears down extensions, giving this a real
     * chance to flush pending writes.
     */
    protected void onCdiDestroy(@Observes ShutdownEvent ev) {
        shutdown();
    }

    /**
     * Perform scheduled persistence of all data.
     * Called automatically by the scheduler.
     */
    private void scheduledPersist() {
        if (isShuttingDown.get()) {
            logger.debug("Skipping scheduled persistence - shutdown in progress");
            return;
        }

        try {
            persist();
            logger.debug("{} scheduled persistence completed", serviceName);
        } catch (Exception e) {
            logger.error("{} error during scheduled persistence: ", serviceName, e);
        }
    }

    /**
     * Persist all data to disk.
     * Subclasses must implement this to define their persistence strategy.
     * This method should handle test mode and shutdown checks internally using
     * {@link #shouldSkipPersistence()}.
     */
    protected abstract void persist();

    /**
     * Load all data from the disk.
     * Subclasses must implement this to define their loading strategy.
     */
    protected abstract void load();

    /**
     * Check if persistence should be skipped (e.g. due to test mode or shutdown).
     *
     * @return true if persistence should be skipped
     */
    protected boolean shouldSkipPersistence() {
        return testModeEnabled || isShuttingDown.get();
    }

    /**
     * Run a read action against a short-lived EntityManager, returning the result.
     * Returns null on failure — callers should treat null as "not found" and log if needed.
     */
    protected <T> T jpaRead(Function<EntityManager, T> action) {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            return action.apply(em);
        } catch (Exception e) {
            logger.error("{} JPA read failed", serviceName, e);
            return null;
        }
    }

    /**
     * Run a write action in its own transaction, rolling back on failure.
     * In test mode the write is skipped and treated as successful — the in-memory
     * state is authoritative there.
     *
     * @return true if the write committed (or was skipped in test mode); false if it
     * failed and was rolled back — callers that mutated in-memory state first should
     * revert it to stay consistent with the database.
     */
    protected boolean jpaWrite(Consumer<EntityManager> action) {
        if (testModeEnabled) return true;
        try (EntityManager em = JpaFactory.createEntityManager()) {
            try {
                em.getTransaction().begin();
                action.accept(em);
                em.getTransaction().commit();
                return true;
            } catch (Exception e) {
                logger.error("{} JPA write failed", serviceName, e);
                try {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                } catch (Exception rollbackError) {
                    logger.error("{} rollback failed", serviceName, rollbackError);
                }
                return false;
            }
        }
    }

    /**
     * Persist first, then publish the in-memory mutation. Test mode still applies
     * the memory mutation because no database write is expected there.
     */
    protected boolean jpaWriteThenMutate(Consumer<EntityManager> action, Runnable mutation) {
        if (testModeEnabled || jpaWrite(action)) {
            mutation.run();
            return true;
        }
        return false;
    }

    /**
     * For APIs that must mutate an existing object before it can be saved, roll
     * the local state back if the database write fails.
     */
    protected boolean jpaWriteWithRollback(Runnable mutation, Consumer<EntityManager> action, Runnable rollback) {
        mutation.run();
        if (testModeEnabled || jpaWrite(action)) {
            return true;
        }
        rollback.run();
        return false;
    }

    protected void requireJpaWrite(Consumer<EntityManager> action) {
        if (!jpaWrite(action)) {
            throw new IllegalStateException(serviceName + " JPA write failed");
        }
    }

    /**
     * Like {@link #jpaWrite}, but always executes — even in test mode. For services
     * with no in-memory cache of their own (reads go straight to JPA on every call,
     * e.g. DeckService), skipping the write in test mode would leave nothing for a
     * subsequent read to find; the H2 test database itself is the only state such a
     * service has, so it isn't optional there the way it is for write-through caches.
     */
    protected boolean jpaWriteAlways(Consumer<EntityManager> action) {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            try {
                em.getTransaction().begin();
                action.accept(em);
                em.getTransaction().commit();
                return true;
            } catch (Exception e) {
                logger.error("{} JPA write failed", serviceName, e);
                try {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                } catch (Exception rollbackError) {
                    logger.error("{} rollback failed", serviceName, rollbackError);
                }
                return false;
            }
        }
    }

    protected void requireJpaWriteAlways(Consumer<EntityManager> action) {
        if (!jpaWriteAlways(action)) {
            throw new IllegalStateException(serviceName + " JPA write failed");
        }
    }

    /**
     * Gracefully shutdown the service, persisting all data and stopping the scheduler.
     * This should be called from a ServletContextListener, not a JVM shutdown hook.
     */
    public void shutdown() {
        // Skip shutdown in test mode
        if (testModeEnabled) {
            logger.info("{} shutdown skipped - test mode enabled", serviceName);
            return;
        }

        // Prevent multiple shutdown calls
        if (!isShuttingDown.compareAndSet(false, true)) {
            logger.warn("{} shutdown already in progress", serviceName);
            return;
        }

        try {
            logger.info("Starting {} shutdown...", serviceName);

            // Shutdown the scheduler first
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    logger.warn("{} scheduler did not terminate gracefully", serviceName);
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                logger.warn("{} scheduler shutdown interrupted", serviceName);
            }

            // Perform final persistence BEFORE classloader stops
            logger.info("Performing final persistence for {}...", serviceName);
            
            // Temporarily allow saves for explicit shutdown save
            isShuttingDown.set(false);
            persist();
            isShuttingDown.set(true);

            // Additional clean-up
            performAdditionalCleanup();

            logger.info("{} shutdown completed.", serviceName);
        } catch (Exception e) {
            logger.error("Error during {} shutdown: ", serviceName, e);
        }
    }

    /**
     * Perform any additional clean-up during shutdown.
     * Subclasses can override this to add custom clean-up logic.
     */
    protected void performAdditionalCleanup() {
        // Default: no additional cleanup
    }

    /**
     * Check if test mode is enabled.
     *
     * @return true if test mode is enabled
     */
    protected boolean isTestModeEnabled() {
        return testModeEnabled;
    }

    /**
     * Check if the service is currently shutting down.
     *
     * @return true if shutdown is in progress
     */
    protected boolean isShuttingDown() {
        return isShuttingDown.get();
    }
}
