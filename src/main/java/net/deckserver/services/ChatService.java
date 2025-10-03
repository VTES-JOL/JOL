package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.TurnData;
import net.deckserver.storage.json.game.TurnHistory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChatService extends PersistedService {

    private static final ChatService INSTANCE = new ChatService();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, GameModel> gmap = new ConcurrentHashMap<>();

    private final LoadingCache<String, TurnHistory> historyCache;

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private ChatService() {
        super("ChatService", 5); // 5 minute persistence interval

        this.historyCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .removalListener((String key, TurnHistory history, RemovalCause cause) -> {
                    // Don't save during shutdown - it's handled explicitly
                    if (!isShuttingDown()) {
                        saveHistory(key, history);
                    }
                })
                .build(this::loadHistory);
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        Map<String, TurnHistory> snapshot = historyCache.asMap();
        int persistedCount = 0;

        logger.debug("Starting persistence of {} cached histories", snapshot.size());

        for (Map.Entry<String, TurnHistory> entry : snapshot.entrySet()) {
            String gameId = entry.getKey();
            TurnHistory history = entry.getValue();

            if (history != null && history.getTurns() != null && !history.getTurns().isEmpty()) {
                saveHistory(gameId, history);
                persistedCount++;
            }
        }

        if (persistedCount > 0) {
            logger.info("Persisted {} histories", persistedCount);
        }
    }

    @Override
    protected void load() {
        // ChatService loads on-demand via Caffeine cache
        // No bulk loading needed
    }

    private void saveHistory(String gameId, TurnHistory history) {
        if (shouldSkipPersistence()) {
            return;
        }

        try {
            if (history == null || history.getTurns() == null || history.getTurns().isEmpty()) {
                logger.debug("Skipping save for {} - history is empty", gameId);
                return;
            }

            logger.debug("Saving history for {} with {} turns", gameId, history.getTurns().size());
            Path historyPath = Paths.get(getBasePath(), "games", gameId, "history.json");
            objectMapper.writeValue(historyPath.toFile(), history.getTurns());
            logger.debug("Successfully saved history for {}", gameId);
        } catch (Exception e) {
            logger.error("Unable to save history for {}", gameId, e);
        }
    }

    private TurnHistory loadHistory(String gameId) {
        try {
            Path historyPath = Paths.get(getBasePath(), "games", gameId, "history.json");
            List<TurnData> turns = objectMapper.readValue(
                    historyPath.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TurnData.class)
            );
            return new TurnHistory(turns);
        } catch (Exception e) {
            return new TurnHistory();
        }
    }

    @Override
    protected void performAdditionalCleanup() {
        historyCache.invalidateAll();
        historyCache.cleanUp();
    }

    @Override
    public void clearCache() {
        logger.debug("Clearing ChatService cache");
        historyCache.invalidateAll();
    }

    public static synchronized void subscribe(String gameId, GameModel model) {
        gmap.put(gameId, model);
    }

    public static synchronized List<String> getTurns(String gameId) {
        return INSTANCE.historyCache.get(gameId).getTurnLabels();
    }

    public static synchronized List<ChatData> getTurn(String gameId, String turnLabel) {
        return INSTANCE.historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static synchronized List<ChatData> getChats(String gameId) {
        String turnLabel = INSTANCE.historyCache.get(gameId).getCurrentTurnLabel();
        return INSTANCE.historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static synchronized String getCurrentTurn(String gameId) {
        return INSTANCE.historyCache.get(gameId).getCurrentTurn();
    }

    public static synchronized void addTurn(String gameId, String player, String turnId) {
        INSTANCE.historyCache.get(gameId).addTurn(player, turnId);
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
        INSTANCE.historyCache.get(gameId).addChat(chat);
        gmap.get(gameId).addChat(chat);
    }
}