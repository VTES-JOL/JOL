package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.entity.GlobalChatEntity;
import net.deckserver.jpa.repository.GlobalChatRepository;
import net.deckserver.ws.WebSocketRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalChatService extends PersistedService {

    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;

    private static final Path PERSISTENCE_PATH = DataPaths.path("chats.json");
    private static final GlobalChatRepository globalChatRepository = new GlobalChatRepository();
    private static final GlobalChatService INSTANCE = new GlobalChatService();

    private List<ChatEntryBean> chats = new ArrayList<>();
    private final Set<PlayerModel> playerModels = new HashSet<>();

    private GlobalChatService() {
        super("GlobalChatService", 5);
        load();
    }

    public static  void subscribe(PlayerModel model) {
        INSTANCE.playerModels.add(model);
    }

    public static  void chat(String player, String message) {
        String sanitize = ParserService.sanitizeText(message);
        String parsedMessage = ParserService.parseGlobalChat(sanitize);
        ChatEntryBean chatEntryBean = new ChatEntryBean(player, parsedMessage);
        INSTANCE.chats.add(chatEntryBean);
        if (INSTANCE.chats.size() > CHAT_STORAGE) {
            INSTANCE.chats = INSTANCE.chats.subList(CHAT_DISCARD, CHAT_STORAGE);
        }
        INSTANCE.playerModels.forEach(playerModel -> playerModel.chat(chatEntryBean));
        WebSocketRegistry.notifyMain();
        if (JpaFactory.isEnabled()) {
            try (EntityManager em = JpaFactory.createEntityManager()) {
                em.getTransaction().begin();
                globalChatRepository.insert(em, chatEntryBean);
                em.getTransaction().commit();
            } catch (Exception e) {
                INSTANCE.logger.error("JPA write failed for global chat", e);
            }
        }
    }

    public static  List<ChatEntryBean> getChats() {
        return INSTANCE.chats;
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
        if (JpaFactory.isEnabled()) {
            try (EntityManager em = JpaFactory.createEntityManager()) {
                em.getTransaction().begin();
                globalChatRepository.trim(em);
                em.getTransaction().commit();
            } catch (Exception e) {
                logger.error("JPA trim failed for global chat", e);
            }
        }
    }

    @Override
    protected void load() {
        if (JpaFactory.isEnabled()) {
            try (EntityManager em = JpaFactory.createEntityManager()) {
                List<GlobalChatEntity> recent = globalChatRepository.findRecent(em, CHAT_STORAGE);
                // findRecent returns DESC order; restore chronological order for the in-memory list
                chats.addAll(recent.reversed().stream()
                        .map(e -> new ChatEntryBean(e.getPlayerName(), e.getMessage()))
                        .collect(Collectors.toCollection(ArrayList::new)));
                logger.info("Loaded {} chat entries from JPA", chats.size());
            } catch (Exception e) {
                logger.error("JPA load failed for GlobalChatService", e);
            }
            return;
        }
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing chat file found");
            return;
        }
        try {
            List<ChatEntryBean> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {});
            chats.addAll(loaded);
            logger.info("Loaded {} chat entries", chats.size());
        } catch (IOException e) {
            logger.error("Unable to load chat.", e);
        }
    }
}
