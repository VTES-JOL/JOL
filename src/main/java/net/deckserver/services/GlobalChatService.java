package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.ws.WebSocketRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class GlobalChatService extends PersistedService {

    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;

    private static final Path PERSISTENCE_PATH = DataPaths.path("chats.json");
    private static final GlobalChatService INSTANCE = new GlobalChatService();
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
        INSTANCE.chats.add(chatEntryBean);
        if (INSTANCE.chats.size() > CHAT_STORAGE) {
            INSTANCE.chats = new ArrayList<>(INSTANCE.chats.subList(CHAT_DISCARD, CHAT_STORAGE));
        }
        WebSocketRegistry.notifyInvalidate(List.of("nav"), excludeClientId);
        WebSocketRegistry.notifyInvalidate(List.of("main-chat"), excludeClientId);
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

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} chat data", chats.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), chats);
            logger.debug("Successfully persisted chat data");
        } catch (IOException e) {
            logger.error("Unable to save chat data", e);
        }
    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing chat file found");
            return;
        }

        try {
            List<ChatEntryBean> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {
            });
            chats.addAll(loaded);
            logger.info("Loaded {} chat entries", chats.size());
        } catch (IOException e) {
            logger.error("Unable to load chat.", e);
        }
    }
}
