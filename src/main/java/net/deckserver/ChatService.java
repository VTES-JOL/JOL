package net.deckserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.TurnData;
import net.deckserver.storage.json.game.TurnHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String basePath = System.getenv("JOL_DATA");
    private static final boolean TEST_MODE_ENABLED = System.getenv().getOrDefault("ENABLE_TEST_MODE", "false").equals("true");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, GameModel> gmap = new ConcurrentHashMap<>();
    private static final Cleaner cleaner = Cleaner.create();
    private static final Cleaner.Cleanable cleanable;
    private static volatile boolean isShuttingDown = false;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "ChatService-Persistence-Scheduler");
        thread.setDaemon(true);
        return thread;
    });
    private static final LoadingCache<String, TurnHistory> historyCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((String key, TurnHistory history, RemovalCause cause) -> {
                // Don't save during shutdown - it's handled explicitly
                if (!isShuttingDown) {
                    saveHistory(key, history);
                }
            })
            .build(ChatService::loadHistory);

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        cleanable = cleaner.register(ChatService.class, new CacheCleanupAction(historyCache));
        
        // Start scheduled persistence task (every 5 minutes)
        if (!TEST_MODE_ENABLED) {
            scheduler.scheduleAtFixedRate(
                    ChatService::persistAllCachedHistories,
                    5, // Initial delay
                    5, // Period
                    TimeUnit.MINUTES
            );
            logger.info("Scheduled persistence task started (every 5 minutes)");
        }
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down ChatService...");
            shutdown();
        }, "ChatService-Shutdown-Hook"));
    }

    private static synchronized TurnHistory loadHistory(String gameId) {
        try {
            Path historyPath = Paths.get(basePath, "games", gameId, "history.json");
            List<TurnData> turns = objectMapper.readValue(
                    historyPath.toFile(), objectMapper.getTypeFactory().constructCollectionType(List.class, TurnData.class)
            );
            return new TurnHistory(turns);
        } catch (Exception e) {
            return new TurnHistory();
        }
    }

    private static synchronized void saveHistory(String gameId, TurnHistory history) {
        // Don't save in test mode
        if (TEST_MODE_ENABLED) {
            logger.debug("Skipping save for {} - test mode enabled", gameId);
            return;
        }

        // Don't save during or after shutdown
        if (isShuttingDown) {
            logger.debug("Skipping save for {} - shutdown in progress", gameId);
            return;
        }

        try {
            // Don't save if history is null or has no turns
            if (history == null || history.getTurns() == null || history.getTurns().isEmpty()) {
                logger.debug("Skipping save for {} - history is empty", gameId);
                return;
            }

            logger.debug("Saving history for {} with {} turns", gameId, history.getTurns().size());
            Path historyPath = Paths.get(basePath, "games", gameId, "history.json");
            objectMapper.writeValue(historyPath.toFile(), history.getTurns());
            logger.debug("Successfully saved history for {}", gameId);
        } catch (Exception e) {
            logger.error("Unable to save history for {}", gameId, e);
        }
    }

    /**
     * Periodically persist all cached histories to disk.
     * This reduces data loss in case of JVM crash or unexpected termination.
     */
    private static void persistAllCachedHistories() {
        if (isShuttingDown) {
            logger.debug("Skipping scheduled persistence - shutdown in progress");
            return;
        }

        try {
            Map<String, TurnHistory> snapshot = historyCache.asMap();
            int persistedCount = 0;
            
            logger.debug("Starting scheduled persistence of {} cached histories", snapshot.size());
            
            for (Map.Entry<String, TurnHistory> entry : snapshot.entrySet()) {
                String gameId = entry.getKey();
                TurnHistory history = entry.getValue();
                
                if (history != null && history.getTurns() != null && !history.getTurns().isEmpty()) {
                    saveHistory(gameId, history);
                    persistedCount++;
                }
            }
            
            if (persistedCount > 0) {
                logger.info("Scheduled persistence completed: {} histories saved", persistedCount);
            }
        } catch (Exception e) {
            logger.error("Error during scheduled persistence: ", e);
        }
    }

    public static synchronized void subscribe(String gameId, GameModel model) {
        gmap.put(gameId, model);
    }

    public static synchronized void unsubscribe(String gameId) {
        gmap.remove(gameId);
    }

    public static synchronized List<String> getTurns(String gameId) {
        return historyCache.get(gameId).getTurnLabels();
    }

    public static synchronized List<ChatData> getTurn(String gameId, String turnLabel) {
        return historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static synchronized List<ChatData> getChats(String gameId) {
        String turnLabel = historyCache.get(gameId).getCurrentTurnLabel();
        return historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static synchronized String getCurrentTurn(String gameId) {
        return historyCache.get(gameId).getCurrentTurn();
    }

    public static synchronized void addTurn(String gameId, String player, String turnId) {
        historyCache.get(gameId).addTurn(player, turnId);
        gmap.get(gameId).clearChats();
    }

    public static synchronized void sendMessage(String gameId, String source, String message) {
        ChatData chatData = new ChatData(message, source, null);
        sendChat(gameId, chatData);
    }

    public static synchronized void sendJudgeMessage(String gameId, String source, String message) {
        ChatData chatData = new ChatData(message, "Judge - " + source, null);
        sendChat(gameId, chatData);
    }

    public static synchronized void sendCommand(String gameId, String source, String message, String... command) {
        ChatData chatData = new ChatData(message, source, String.join(" ", command));
        sendChat(gameId, chatData);
    }

    public static synchronized void sendSystemMessage(String gameId, String message) {
        ChatData chatData = new ChatData(message, "SYSTEM", null);
        sendChat(gameId, chatData);
    }

    private static synchronized void sendChat(String gameId, ChatData chat) {
        historyCache.get(gameId).addChat(chat);
        gmap.get(gameId).addChat(chat);
    }

    /**
     * Manually trigger cache persistence and cleanup.
     * Useful for shutdown hooks or explicit cleanup scenarios.
     */
    protected static void shutdown() {
        // Don't persist in test mode
        if (TEST_MODE_ENABLED) {
            logger.info("ChatService shutdown skipped - test mode enabled");
            return;
        }

        try {
            logger.info("Starting ChatService shutdown...");
            isShuttingDown = true;

            // Shutdown the scheduler first
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Save all entries explicitly
            Map<String, TurnHistory> snapshot = new ConcurrentHashMap<>(historyCache.asMap());
            logger.info("Saving {} history entries...", snapshot.size());

            // Temporarily allow saves for explicit shutdown save
            isShuttingDown = false;
            snapshot.forEach((gameId, history) -> {
                if (history != null && history.getTurns() != null && !history.getTurns().isEmpty()) {
                    logger.debug("Shutdown: saving {} with {} turns", gameId, history.getTurns().size());
                    saveHistory(gameId, history);
                } else {
                    logger.debug("Shutdown: skipping empty history for {}", gameId);
                }
            });
            // Re-enable shutdown flag to prevent any further saves
            isShuttingDown = true;

            // Clear the cache - removal listener will be suppressed
            historyCache.invalidateAll();
            historyCache.cleanUp();

            logger.info("ChatService shutdown completed.");
        } catch (Exception e) {
            logger.error("Error during ChatService shutdown: ", e);
        }
    }

    /**
     * Clear the cache - useful for resetting state between tests
     */
    public static void clearCache() {
        logger.debug("Clearing ChatService cache");
        historyCache.invalidateAll();
    }

    /**
     * Cleanup action that persists all cached items before the ChatService is garbage collected.
     * This class must not hold a reference to ChatService or historyCache directly to avoid
     * preventing garbage collection.
     */
    private record CacheCleanupAction(LoadingCache<String, TurnHistory> cache) implements Runnable {

        @Override
        public void run() {
            try {
                // Persist all items in the cache before cleanup
                cache.asMap().forEach((gameId, history) -> {
                    try {
                        saveHistory(gameId, history);
                    } catch (Exception e) {
                        logger.error("Failed to persist history for game {} during cleanup: ", gameId, e);
                    }
                });
                logger.error("ChatService cache cleanup completed. Persisted {} entries.", cache.estimatedSize());
            } catch (Exception e) {
                logger.error("Error during cache cleanup: ", e);
            }
        }
    }
}
