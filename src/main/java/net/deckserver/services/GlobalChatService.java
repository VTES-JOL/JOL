package net.deckserver.services;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.entity.GlobalChatEntity;
import net.deckserver.jpa.repository.GlobalChatRepository;
import net.deckserver.rest.bean.ChatEntryBean;
import net.deckserver.ws.WebSocketRegistry;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class GlobalChatService extends PersistedService {

    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;

    private static final GlobalChatRepository globalChatRepository = new GlobalChatRepository();
    private static final GlobalChatService INSTANCE = new GlobalChatService();
    private static final Map<String, String> lastSeenByPlayer = new ConcurrentHashMap<>();
    private List<ChatEntryBean> chats = new ArrayList<>();

    private GlobalChatService() {
        super("GlobalChatService", 5);
        load();
    }

    public static void chat(String player, String message) {
        chat(player, message, null);
    }

    /**
     * excludeClientId skips notifying the caller's own WS session (see
     * WebSocketRegistry.notifyInvalidate) — REST callers who already have the
     * fresh state from their own response should pass it; background jobs
     * and other non-REST callers (PublicGameBuilder, GameCleanUp, ...) have
     * no client to exclude and use the two-arg overload above.
     */
    public static synchronized void chat(String player, String message, String excludeClientId) {
        String sanitize = ParserService.sanitizeText(message);
        String parsedMessage = ParserService.parseGlobalChat(sanitize);
        ChatEntryBean chatEntryBean = new ChatEntryBean(player, parsedMessage);
        if (INSTANCE.jpaWriteThenMutate(
                em -> globalChatRepository.insert(em, chatEntryBean),
                () -> {
                    INSTANCE.chats.add(chatEntryBean);
                    if (INSTANCE.chats.size() > CHAT_STORAGE) {
                        INSTANCE.chats = new ArrayList<>(INSTANCE.chats.subList(CHAT_DISCARD, CHAT_STORAGE));
                    }
                })) {
            WebSocketRegistry.notifyInvalidate(List.of("nav"), excludeClientId);
            WebSocketRegistry.notifyInvalidate(List.of("main-chat"), excludeClientId);
        }
    }

    /** Most recent chat entries, independent of any player's read cursor — for populating history on first load. */
    public static synchronized List<ChatEntryBean> getRecentChats(int limit) {
        int size = INSTANCE.chats.size();
        int from = Math.max(0, size - limit);
        return new ArrayList<>(INSTANCE.chats.subList(from, size));
    }

    /** Returns chat entries strictly after the given cursor timestamp (ISO offset date-time), or all entries if cursor is null. */
    public static synchronized List<ChatEntryBean> getChatsSince(String cursor) {
        if (cursor == null) {
            return new ArrayList<>(INSTANCE.chats);
        }
        OffsetDateTime cursorTime = OffsetDateTime.parse(cursor, ISO_OFFSET_DATE_TIME);
        return INSTANCE.chats.stream()
                .filter(entry -> OffsetDateTime.parse(entry.getTimestamp(), ISO_OFFSET_DATE_TIME).isAfter(cursorTime))
                .collect(Collectors.toList());
    }

    /** Non-destructive check for whether any chat entry exists after the given cursor timestamp. */
    public static synchronized boolean hasChatsSince(String cursor) {
        if (INSTANCE.chats.isEmpty()) {
            return false;
        }
        if (cursor == null) {
            return true;
        }
        OffsetDateTime cursorTime = OffsetDateTime.parse(cursor, ISO_OFFSET_DATE_TIME);
        OffsetDateTime lastTime = OffsetDateTime.parse(INSTANCE.chats.get(INSTANCE.chats.size() - 1).getTimestamp(), ISO_OFFSET_DATE_TIME);
        return lastTime.isAfter(cursorTime);
    }

    /** This player's chat delta since their last read, advancing their cursor to match — replaces PlayerModel.getChat(). */
    public static synchronized List<ChatEntryBean> getUnseenChats(String player) {
        List<ChatEntryBean> result = getChatsSince(lastSeenByPlayer.get(player));
        markSeen(player, result);
        return result;
    }

    /** Advances this player's read cursor to the last entry in the batch — replaces PlayerModel.markChatsSeenThrough(). */
    public static synchronized void markSeen(String player, List<ChatEntryBean> entries) {
        if (!entries.isEmpty()) {
            lastSeenByPlayer.put(player, entries.get(entries.size() - 1).getTimestamp());
        }
    }

    /** Non-destructive check for the nav unread badge — replaces PlayerModel.hasChats(). */
    public static synchronized boolean hasUnseenChats(String player) {
        return hasChatsSince(lastSeenByPlayer.get(player));
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }
        requireJpaWrite(globalChatRepository::trim);
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            List<GlobalChatEntity> recent = globalChatRepository.findRecent(em, CHAT_STORAGE);
            chats.addAll(recent.reversed().stream()
                    .map(GlobalChatService::toChatEntryBean)
                    .collect(Collectors.toCollection(ArrayList::new)));
            logger.info("Loaded {} chat entries from JPA", chats.size());
        } catch (Exception e) {
            logger.error("JPA load failed for GlobalChatService", e);
        }
    }

    // Preserves the original posted_at rather than the (player, message) constructor's
    // implicit "now" timestamp — getChatsSince/hasChatsSince compare on it directly.
    private static ChatEntryBean toChatEntryBean(GlobalChatEntity entity) {
        ChatEntryBean bean = new ChatEntryBean();
        bean.setPlayer(entity.getPlayerName());
        bean.setMessage(entity.getMessage());
        bean.setTimestamp(entity.getPostedAt().truncatedTo(ChronoUnit.SECONDS).format(ISO_OFFSET_DATE_TIME));
        return bean;
    }
}
