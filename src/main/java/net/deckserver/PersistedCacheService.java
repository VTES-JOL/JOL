package net.deckserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base class for services that require scheduled persistence, graceful shutdown,
 * and test mode support.
 * 
 * @param <K> The key type for cached entries
 * @param <V> The value type for cached entries
 */
public abstract class PersistedCacheService<K, V> {

    protected final Logger logger;
    protected final String serviceName;
    protected final boolean testModeEnabled;
    protected volatile boolean isShuttingDown = false;
    protected final ScheduledExecutorService scheduler;
    protected static final Cleaner cleaner = Cleaner.create();
    protected final Cleaner.Cleanable cleanable;

    /**
     * Constructor that initializes the service with scheduled persistence.
     *
     * @param serviceName Name of the service (used for logging and thread naming)
     * @param persistenceIntervalMinutes How often to persist data (in minutes)
     */
    protected PersistedCacheService(String serviceName, int persistenceIntervalMinutes) {
        this.serviceName = serviceName;
        this.logger = LoggerFactory.getLogger(getClass());
        this.testModeEnabled = System.getenv().getOrDefault("ENABLE_TEST_MODE", "false").equals("true");

        // Create scheduler with daemon thread
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, serviceName + "-Persistence-Scheduler");
            thread.setDaemon(true);
            return thread;
        });

        // Register a cleanup action with Cleaner
        this.cleanable = cleaner.register(this, new CacheCleanupAction<>(this));

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

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down {}...", serviceName);
            shutdown();
        }, serviceName + "-Shutdown-Hook"));
    }

    /**
     * Perform scheduled persistence of all cached data.
     * Called automatically by the scheduler.
     */
    private void scheduledPersist() {
        if (isShuttingDown) {
            logger.debug("Skipping scheduled persistence - shutdown in progress");
            return;
        }

        try {
            int persistedCount = persistAllEntries();

            if (persistedCount > 0) {
                logger.info("{} scheduled persistence completed: {} entries saved",
                        serviceName, persistedCount);
            }
        } catch (Exception e) {
            logger.error("{} error during scheduled persistence: ", serviceName, e);
        }
    }

    /**
     * Persist all cached entries to disk.
     * Subclasses must implement this to define how to iterate and save their data.
     *
     * @return The number of entries successfully persisted
     */
    protected abstract int persistAllEntries();

    /**
     * Save a single entry to disk.
     * Subclasses must implement this to define how to save individual entries.
     *
     * @param key The entry key
     * @param value The entry value
     */
    protected abstract void saveEntry(K key, V value);

    /**
     * Load a single entry from disk.
     * Subclasses must implement this to define how to load individual entries.
     *
     * @param key The entry key
     * @return The loaded value, or a default/empty value if not found
     */
    protected abstract V loadEntry(K key);

    /**
     * Check if an entry should be persisted.
     * Subclasses can override this to add custom validation logic.
     *
     * @param key The entry key
     * @param value The entry value
     * @return true if the entry should be persisted, false otherwise
     */
    protected boolean shouldPersist(K key, V value) {
        return value != null;
    }

    /**
     * Check if persistence should be skipped (e.g., due to test mode or shutdown).
     *
     * @return true if persistence should be skipped
     */
    protected boolean shouldSkipPersistence() {
        return testModeEnabled || isShuttingDown;
    }

    /**
     * Gracefully shutdown the service, persisting all data and stopping the scheduler.
     */
    protected void shutdown() {
        // Skip shutdown in test mode
        if (testModeEnabled) {
            logger.info("{} shutdown skipped - test mode enabled", serviceName);
            return;
        }

        try {
            logger.info("Starting {} shutdown...", serviceName);
            isShuttingDown = true;

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

            // Perform final persistence
            logger.info("Performing final persistence for {}...", serviceName);

            // Temporarily allow saves for explicit shutdown save
            isShuttingDown = false;
            int persistedCount = persistAllEntries();
            logger.info("Persisted {} entries during {} shutdown", persistedCount, serviceName);

            // Re-enable shutdown flag to prevent any further saves
            isShuttingDown = true;

            // Additional cleanup
            performAdditionalCleanup();

            logger.info("{} shutdown completed.", serviceName);
        } catch (Exception e) {
            logger.error("Error during {} shutdown: ", serviceName, e);
        }
    }

    /**
     * Perform any additional cleanup during shutdown.
     * Subclasses can override this to add custom cleanup logic.
     */
    protected void performAdditionalCleanup() {
        // Default: no additional cleanup
    }

    /**
     * Clear all cached data.
     * Subclasses must implement this to define how to clear their cache.
     */
    protected abstract void clearCache();

    /**
     * Get the base path for data storage.
     *
     * @return The base path from JOL_DATA environment variable
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
        return isShuttingDown;
    }

    /**
     * Cleanup action that persists all cached items before the service is garbage collected.
     * This class must not hold a direct reference to the service instance to avoid
     * preventing garbage collection.
     *
     * @param <K> The key type for cached entries
     * @param <V> The value type for cached entries
     */
    protected static class CacheCleanupAction<K, V> implements Runnable {
        private final String serviceName;
        private final Logger logger;

        // Store a functional interface instead of the service instance
        private final PersistFunction persistFunction;

        protected CacheCleanupAction(PersistedCacheService<K, V> service) {
            this.serviceName = service.serviceName;
            this.logger = service.logger;
            this.persistFunction = service::persistAllEntries;
        }

        @Override
        public void run() {
            try {
                int persistedCount = persistFunction.persist();
                logger.info("{} cache cleanup completed. Persisted {} entries.",
                        serviceName, persistedCount);
            } catch (Exception e) {
                logger.error("{} error during cache cleanup: ", serviceName, e);
            }
        }

        @FunctionalInterface
        protected interface PersistFunction {
            int persist();
        }
    }
}