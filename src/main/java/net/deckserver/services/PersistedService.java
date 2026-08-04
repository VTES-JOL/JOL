package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

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
    protected final Logger logger;
    protected final String serviceName;
    protected final boolean testModeEnabled;
    protected final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    protected final ScheduledExecutorService scheduler;
    protected static final Cleaner cleaner = Cleaner.create();
    protected final Cleaner.Cleanable cleanable;

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
        this.testModeEnabled = System.getenv().getOrDefault("ENABLE_TEST_MODE", "false").equals("true");

        // Create scheduler with daemon thread
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, serviceName + "-Persistence-Scheduler");
            thread.setDaemon(true);
            return thread;
        });

        // Register a clean-up action with Cleaner
        this.cleanable = cleaner.register(this, new CleanupAction(this));

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

            // Perform final JPA flush BEFORE classloader stops
            logger.info("Performing final persistence for {}...", serviceName);
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

    /**
     * Clean-up action that persists all data before the service is rubbish collected.
     * This class must not hold a direct reference to the service instance to avoid
     * preventing garbage collection.
     */
    protected static class CleanupAction implements Runnable {
        private final String serviceName;
        private final Logger logger;
        private final PersistFunction persistFunction;

        protected CleanupAction(PersistedService service) {
            this.serviceName = service.serviceName;
            this.logger = service.logger;
            this.persistFunction = service::persist;
        }

        @Override
        public void run() {
            try {
                persistFunction.persist();
                logger.info("{} cache cleanup completed.", serviceName);
            } catch (Exception e) {
                logger.error("{} error during cache cleanup: ", serviceName, e);
            }
        }

        @FunctionalInterface
        protected interface PersistFunction {
            void persist();
        }
    }
}
