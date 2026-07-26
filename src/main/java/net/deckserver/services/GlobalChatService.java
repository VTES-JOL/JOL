package net.deckserver.services;

import jakarta.persistence.EntityManager;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.entity.GlobalChatEntity;
import net.deckserver.jpa.repository.GlobalChatRepository;
import net.deckserver.ws.WebSocketRegistry;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class GlobalChatService extends PersistedService {

    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;

    private static final GlobalChatRepository globalChatRepository = new GlobalChatRepository();
    private static final GlobalChatService INSTANCE = new GlobalChatService();
    private List<ChatEntryBean> chats = new ArrayList<>();

    private GlobalChatService() {
        super("GlobalChatService", 5);
        load();
    }

    public static synchronized void chat(String player, String message) {
        String sanitize = ParserService.sanitizeText(message);
        String parsedMessage = ParserService.parseGlobalChat(sanitize);
        ChatEntryBean chatEntryBean = new ChatEntryBean(player, parsedMessage);
        INSTANCE.chats.add(chatEntryBean);
        if (INSTANCE.chats.size() > CHAT_STORAGE) {
            INSTANCE.chats = new ArrayList<>(INSTANCE.chats.subList(CHAT_DISCARD, CHAT_STORAGE));
        }
        WebSocketRegistry.notifyMain();
        INSTANCE.jpaWrite(em -> globalChatRepository.insert(em, chatEntryBean));
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
        jpaWrite(globalChatRepository::trim);
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            List<GlobalChatEntity> recent = globalChatRepository.findRecent(em, CHAT_STORAGE);
            chats.addAll(recent.reversed().stream()
                    .map(e -> new ChatEntryBean(e.getPlayerName(), e.getMessage()))
                    .collect(Collectors.toCollection(ArrayList::new)));
            logger.info("Loaded {} chat entries from JPA", chats.size());
        } catch (Exception e) {
            logger.error("JPA load failed for GlobalChatService", e);
        }
    }
}
