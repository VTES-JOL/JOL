package net.deckserver.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameChatMessageRepository;
import net.deckserver.jpa.repository.GameCommandErrorRepository;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.CommandErrorData;
import net.deckserver.storage.json.game.TurnData;
import net.deckserver.storage.json.game.TurnHistory;

import java.util.ArrayList;
import java.util.List;
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
    // only intercept methods. Confirmed the hard way: login worked against an
    // empty in-memory player map despite "Loaded 9 players from JPA" logging
    // successfully at startup — two different objects. @Singleton is a CDI
    // pseudo-scope: no proxy, Instance.get() returns the real instance.
    private static ChatService instance() {
        return resolve(ChatService.class, ChatService::new);
    }

    private static final GameChatMessageRepository messageRepository = new GameChatMessageRepository();
    private static final GameCommandErrorRepository errorRepository = new GameCommandErrorRepository();

    /**
     * The raw command currently being executed on this thread, if any. Set by
     * {@link #beginInvocation}/{@link #endInvocation} around each
     * {@code DoCommand.doCommand} call in {@code GameModel.submit}, and stamped
     * onto every {@link ChatData} that command produces at {@link #sendChat}.
     * Safe as a ThreadLocal: a submit runs single-threaded under
     * {@code GameModel}'s ReentrantLock, single-node.
     */
    private record Invocation(String issuer, String raw) {}
    private static final ThreadLocal<Invocation> CURRENT_INVOCATION = new ThreadLocal<>();

    // Read accelerator only. Persistence is write-through per message (see sendChat);
    // there is no background flush and eviction just drops the cached copy — the
    // database already holds every row.
    private final LoadingCache<String, TurnHistory> historyCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(this::loadHistory);

    ChatService() {
        super("ChatService", 0); // write-through, no scheduled persistence
    }

    public static PersistedService getInstance() {
        return instance();
    }

    public static void beginInvocation(String issuer, String raw) {
        CURRENT_INVOCATION.set(new Invocation(issuer, raw));
    }

    public static void endInvocation() {
        CURRENT_INVOCATION.remove();
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

    /**
     * Record a command a player submitted that failed to parse or validate.
     * These are NOT chat — they never enter TurnHistory or the normal log — but
     * a judge investigating a misplay can see them (see {@link #getFailedCommands}).
     * Written straight through with {@code jpaWriteAlways} (no in-memory cache,
     * so it must persist even in test mode, like DeckService).
     */
    public static void recordFailedCommand(String gameId, String player, String rawCommand, String errorText) {
        String turnLabel = instance().historyCache.get(gameId).getCurrentTurnLabel();
        instance().jpaWriteAlways(em ->
                errorRepository.insert(em, gameId, turnLabel, player, rawCommand, errorText));
    }

    /** Failed command attempts for a turn — judge-only, gated at the resource. */
    public static List<CommandErrorData> getFailedCommands(String gameId, String turnLabel) {
        List<CommandErrorData> result = instance().jpaRead(em ->
                errorRepository.loadTurn(em, gameId, turnLabel));
        return result != null ? result : new ArrayList<>();
    }

    private static  void sendChat(String gameId, ChatData chat) {
        Invocation inv = CURRENT_INVOCATION.get();
        if (inv != null) {
            if (chat.getInvocation() == null) {
                chat.setInvocation(inv.raw());
            }
            if (chat.getInvocationBy() == null) {
                chat.setInvocationBy(inv.issuer());
            }
        }

        TurnHistory history = instance().historyCache.get(gameId);
        TurnData turn = history.addChat(chat);
        int turnSeq = history.getCurrentTurnIndex();
        int chatSeq = Math.max(0, turn.getChats().size() - 1);
        String turnId = history.getCurrentTurn();
        String player = history.getCurrentPlayer();
        String turnLabel = history.getCurrentTurnLabel();

        boolean ok = instance().jpaWrite(em ->
                messageRepository.insert(em, gameId, turnSeq, chatSeq, turnId, player, turnLabel, chat));
        if (!ok) {
            instance().logger.error("Failed to persist chat line for game {} (turn {})", gameId, turnLabel);
        }
    }

    private TurnHistory loadHistory(String gameId) {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            List<TurnData> turns = messageRepository.load(em, gameId);
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
        // Write-through — nothing to flush. Kept because PersistedService requires it.
    }

    @Override
    protected void load() {
        // ChatService loads on-demand via Caffeine cache. No bulk loading needed.
    }

    @Override
    protected void performAdditionalCleanup() {
        historyCache.invalidateAll();
        historyCache.cleanUp();
    }
}
