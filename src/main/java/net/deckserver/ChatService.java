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
import java.util.concurrent.TimeUnit;

public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String basePath = System.getenv("JOL_DATA");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final LoadingCache<String, TurnHistory> historyCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((String key, TurnHistory history, RemovalCause cause) -> saveHistory(key, history))
            .build(ChatService::loadHistory);
    private static final Map<String, GameModel> gmap = new ConcurrentHashMap<>();

    private static final Cleaner cleaner = Cleaner.create();
    private static final Cleaner.Cleanable cleanable;

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        cleanable = cleaner.register(ChatService.class, new CacheCleanupAction(historyCache));
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
        try {
            logger.debug("Saving history for {}", gameId);
            Path historyPath = Paths.get(basePath, "games", gameId, "history.json");
            objectMapper.writeValue(historyPath.toFile(), history.getTurns());
        } catch (Exception e) {
            logger.error("Unable to save history for {}", gameId, e);
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
        try {
            historyCache.asMap().forEach(ChatService::saveHistory);
            historyCache.invalidateAll();
            historyCache.cleanUp();
        } catch (Exception e) {
            logger.error("Error during ChatService shutdown: ", e);
        }
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
