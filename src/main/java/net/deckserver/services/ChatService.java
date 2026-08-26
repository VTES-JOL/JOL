package net.deckserver.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameChatRepository;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.TurnData;
import net.deckserver.storage.json.game.TurnHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
public class ChatService extends PersistedService {

    // CDI-backed replacement for the old `private static final ChatService
    // INSTANCE = new ChatService()` singleton field — @Startup forces Arc to
    // eagerly create this bean at application startup (same "ready before
    // any caller touches it" guarantee the static field used to provide).
    // @Singleton, not @ApplicationScoped: the latter is a CDI "normal scope",
    // meaning Instance.get() returns a client proxy — fine for method calls,
    // but direct field access (e.g. instance().historyCache) silently reads
    // the proxy's own empty field instead of the real bean's, since proxies
    // only intercept methods. Confirmed the hard way (see
    // quarkus-poc/FINDINGS.md's Phase 3 section: login worked against an
    // empty in-memory player map despite "Loaded 9 players from JPA" logging
    // successfully at startup — two different objects). @Singleton is a CDI
    // pseudo-scope: no proxy, Instance.get() returns the real instance.
    private static ChatService instance() {
        return resolve(ChatService.class, ChatService::new);
    }

    private static final GameChatRepository gameChatRepository = new GameChatRepository();

    private final LoadingCache<String, TurnHistory> historyCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((String key, TurnHistory history, RemovalCause cause) -> {
                // Don't save during shutdown - it's handled explicitly
                if (!isShuttingDown()) {
                    saveHistory(key, history);
                }
            })
            .build(this::loadHistory);

    ChatService() {
        super("ChatService", 5); // 5 minute persistence interval
    }

    public static PersistedService getInstance() {
        return instance();
    }

    public static  List<String> getTurns(String gameId) {
        return instance().historyCache.get(gameId).getTurnLabels();
    }

    public static  List<ChatData> getTurn(String gameId, String turnLabel) {
        return instance().historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    /** The current turn's chat log — used by tests to assert on the latest message. */
    public static  List<ChatData> getChats(String gameId) {
        String turnLabel = instance().historyCache.get(gameId).getCurrentTurnLabel();
        if (turnLabel == null) {
            return new ArrayList<>();
        }
        return instance().historyCache.get(gameId).getTurn(turnLabel).getChats();
    }

    public static  void addTurn(String gameId, String player, String turnId) {
        instance().historyCache.get(gameId).addTurn(player, turnId);
    }

    public static  void sendMessage(String gameId, String source, String message) {
        ChatData chatData = new ChatData(message, source, null);
        sendChat(gameId, chatData);
    }

    public static  void sendJudgeMessage(String gameId, String source, String message) {
        ChatData chatData = new ChatData(message, "Judge - " + source, null);
        sendChat(gameId, chatData);
    }

    public static  void sendCommand(String gameId, String source, String message, String... command) {
        ChatData chatData = new ChatData(message, source, String.join(" ", command));
        sendChat(gameId, chatData);
    }

    public static  void sendSystemMessage(String gameId, String message) {
        ChatData chatData = new ChatData(message, "SYSTEM", null);
        sendChat(gameId, chatData);
    }

    private static  void sendChat(String gameId, ChatData chat) {
        instance().historyCache.get(gameId).addChat(chat);
    }

    private void saveHistory(String gameId, TurnHistory history) {
        if (shouldSkipPersistence()) {
            return;
        }

        if (history == null || history.getTurns() == null || history.getTurns().isEmpty()) {
            logger.debug("Skipping save for {} - history is empty", gameId);
            return;
        }

        logger.debug("Saving history for {} with {} turns", gameId, history.getTurns().size());
        requireJpaWrite(em -> gameChatRepository.save(em, gameId, history.getTurns()));
    }

    private TurnHistory loadHistory(String gameId) {
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

    @Override
    protected void performAdditionalCleanup() {
        historyCache.invalidateAll();
        historyCache.cleanUp();
    }
}
