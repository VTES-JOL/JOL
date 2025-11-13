package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for services that require scheduled persistence, graceful shutdown,
 * and test mode support.
 * <p>
 * This class handles the lifecycle management (scheduling, shutdown, test mode)
 * while allowing subclasses to define their own persistence strategy.
 * <p>
 * NOTE: Shutdown should be triggered via ServletContextListener, not JVM shutdown hooks,
 * to avoid classloader issues in servlet containers.
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
     * @param persistenceIntervalMinutes How often to persist data (in minutes)
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
        if (!testModeEnabled) {
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
     * Get the base path for data storage.
     *
     * @return The base path from the JOL_DATA environment variable
     */
    protected String getBasePath() {
        return System.getenv("JOL_DATA");
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
