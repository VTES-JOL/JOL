package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import jakarta.persistence.EntityManager;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameChatRepository;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.TurnData;
import net.deckserver.storage.json.game.TurnHistory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChatService extends PersistedService {

    private static final ChatService INSTANCE = new ChatService();
    private static final Map<String, GameModel> gmap = new ConcurrentHashMap<>();
    private static final GameChatRepository gameChatRepository = new GameChatRepository();

    private final LoadingCache<String, TurnHistory> historyCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((String key, TurnHistory history, RemovalCause cause) -> {
                if (!isShuttingDown()) {
                    saveHistory(key, history);
                }
            })
            .build(this::loadHistory);

    private ChatService() {
        super("ChatService", 5);
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    public static void subscribe(String gameId, GameModel model) {
        gmap.put(gameId, model);
    }

    public static List<String> getTurns(String gameId) {
        return INSTANCE.historyCache.get(gameId).getTurnLabels();
    }

    public static List<ChatData> getTurn(String gameId, String turnLabel) {
        return INSTANCE.historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static List<ChatData> getChats(String gameId) {
        String turnLabel = INSTANCE.historyCache.get(gameId).getCurrentTurnLabel();
        if (turnLabel == null) {
            return new ArrayList<>();
        }
        return INSTANCE.historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static void addTurn(String gameId, String player, String turnId) {
        INSTANCE.historyCache.get(gameId).addTurn(player, turnId);
        Optional.ofNullable(gmap.get(gameId)).ifPresent(GameModel::clearChats);
    }

    public static void sendMessage(String gameId, String source, String message) {
        sendChat(gameId, new ChatData(message, source, null));
    }

    public static void sendJudgeMessage(String gameId, String source, String message) {
        sendChat(gameId, new ChatData(message, "Judge - " + source, null));
    }

    public static void sendCommand(String gameId, String source, String message, String... command) {
        sendChat(gameId, new ChatData(message, source, String.join(" ", command)));
    }

    public static void sendSystemMessage(String gameId, String message) {
        sendChat(gameId, new ChatData(message, "SYSTEM", null));
    }

    private static void sendChat(String gameId, ChatData chat) {
        INSTANCE.historyCache.get(gameId).addChat(chat);
        Optional.ofNullable(gmap.get(gameId)).ifPresent(model -> model.addChat(chat));
    }

    private void saveHistory(String gameId, TurnHistory history) {
        if (shouldSkipPersistence()) return;
        if (history == null || history.getTurns() == null || history.getTurns().isEmpty()) {
            logger.debug("Skipping save for {} - history is empty", gameId);
            return;
        }
        logger.debug("Saving history for {} with {} turns", gameId, history.getTurns().size());
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            gameChatRepository.save(em, gameId, history.getTurns());
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for chat history {}", gameId, e);
        }
    }

    private TurnHistory loadHistory(String gameId) {
        if (testModeEnabled) {
            Path historyPath = DataPaths.path("games", gameId, "history.json");
            if (Files.exists(historyPath)) {
                try {
                    List<TurnData> turns = objectMapper.readValue(historyPath.toFile(), new TypeReference<>() {});
                    if (!turns.isEmpty()) return new TurnHistory(turns);
                } catch (IOException e) {
                    logger.error("Error reading history fixture for {}", gameId, e);
                }
            }
            return new TurnHistory();
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            List<TurnData> turns = gameChatRepository.load(em, gameId);
            if (!turns.isEmpty()) {
                return new TurnHistory(turns);
            }
        } catch (Exception e) {
            logger.error("JPA load failed for game chat {}", gameId, e);
        }
        return new TurnHistory();
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }
        Map<String, TurnHistory> snapshot = historyCache.asMap();
        int persistedCount = 0;
        for (Map.Entry<String, TurnHistory> entry : snapshot.entrySet()) {
            TurnHistory history = entry.getValue();
            if (history != null && history.getTurns() != null && !history.getTurns().isEmpty()) {
                saveHistory(entry.getKey(), history);
                persistedCount++;
            }
        }
        if (persistedCount > 0) {
            logger.info("Persisted {} histories", persistedCount);
        }
    }

    @Override
    protected void load() {
        // on-demand via Caffeine cache
    }

    @Override
    protected void performAdditionalCleanup() {
        historyCache.invalidateAll();
        historyCache.cleanUp();
    }
}
