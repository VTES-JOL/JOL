package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.dwr.model.PlayerModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalChatService extends PersistedService {

    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;

    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "chats.json");
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
