package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.azam.ulidj.ULID;
import net.deckserver.storage.json.system.PlayerInfo;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerService extends PersistedService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "players.json");
    private static final PlayerService INSTANCE = new PlayerService();

    private final Map<String, PlayerInfo> players = new HashMap<>();

    private PlayerService() {
        super("PlayerService", 5);
        load();
    }

    public static boolean existsPlayer(String name) {
        return name != null && INSTANCE.players.containsKey(name);
    }

    public static boolean registerPlayer(String name, String password, String email) {
        if (existsPlayer(name) || name.isEmpty())
            return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        INSTANCE.players.put(name, new PlayerInfo(name, ULID.random(), email, hash));
        return true;
    }

    public static boolean authenticate(String playerName, String password) {
        if (existsPlayer(playerName)) {
            PlayerInfo playerInfo = loadPlayerInfo(playerName);
            return BCrypt.checkpw(password, playerInfo.getHash());
        } else {
            return false;
        }
    }

    public static synchronized void changePassword(String player, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        loadPlayerInfo(player).setHash(hash);
    }

    public static synchronized void updateProfile(String playerName, String email, String discordID, String veknID) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        playerInfo.setDiscordId(discordID);
        playerInfo.setEmail(email);
        playerInfo.setVeknId(veknID);
    }

    private static PlayerInfo loadPlayerInfo(String playerName) {
        if (INSTANCE.players.containsKey(playerName)) {
            return INSTANCE.players.get(playerName);
        }
        throw new IllegalArgumentException("Player: " + playerName + " was not found.");
    }

    public static PlayerInfo get(String playerName) {
        return loadPlayerInfo(playerName);
    }

    public static Set<String> getPlayers() {
        return INSTANCE.players.keySet();
    }

    public static void remove(String name) {
        INSTANCE.players.remove(name);
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} player data", players.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), players);
            logger.debug("Successfully persisted player data");
        } catch (IOException e) {
            logger.error("Unable to save player data", e);
        }
    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing player file found");
            return;
        }

        try {
            Map<String, PlayerInfo> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {});
            players.putAll(loaded);
            logger.info("Loaded {} players", players.size());
        } catch (IOException e) {
            logger.error("Unable to load players.", e);
        }
    }
}
