package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Streams;
import io.azam.ulidj.ULID;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.storage.json.system.PlayerInfo;
import net.deckserver.storage.json.system.UserSummary;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerService extends PersistedService {

    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "players.json");
    private static final PlayerService INSTANCE = new PlayerService();
    private static final LoadingCache<String, UserSummary> activeUsers = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .refreshAfterWrite(1, TimeUnit.MINUTES)
            .build(PlayerService::generateSummary);
    private static final Predicate<UserSummary> RECENTLY_ONLINE = summary -> OffsetDateTime.parse(summary.getLastOnline()).plusMinutes(30).isAfter(OffsetDateTime.now());
    private final Map<String, PlayerInfo> players = new HashMap<>();

    private PlayerService() {
        super("PlayerService", 5);
        load();
    }

    private static UserSummary generateSummary(String playerName) {
        PlayerInfo playerInfo = get(playerName);
        UserSummary userSummary = new UserSummary();
        userSummary.setName(playerName);
        userSummary.setLastOnline(OffsetDateTime.now());
        userSummary.setRoles(playerInfo.getRoles().stream().map(PlayerRole::name).toList());
        userSummary.setCountry(playerInfo.getCountryCode());
        return userSummary;
    }

    public static List<UserSummary> activeUsers() {
        Map<Boolean, List<UserSummary>> users = activeUsers.asMap().values().stream()
                .sorted(Comparator.comparing(UserSummary::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.partitioningBy(RECENTLY_ONLINE));
        return Streams.concat(users.get(true).stream(), users.get(false).stream()).toList();
    }

    public static void refreshActive(String playerName) {
        activeUsers.get(playerName).setLastOnline(OffsetDateTime.now());
    }

    public static synchronized boolean existsPlayer(String name) {
        return name != null && INSTANCE.players.containsKey(name);
    }

    public static synchronized boolean registerPlayer(String name, String password, String email) {
        if (existsPlayer(name) || name.isEmpty())
            return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        INSTANCE.players.put(name, new PlayerInfo(name, ULID.random(), email, hash));
        return true;
    }

    public static synchronized boolean authenticate(String playerName, String password) {
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

    public static synchronized void updateProfile(String playerName, String email, String discordID, String veknID, String country) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        playerInfo.setDiscordId(discordID);
        playerInfo.setEmail(email);
        playerInfo.setVeknId(veknID);
        playerInfo.setCountryCode(country);
    }

    private static synchronized PlayerInfo loadPlayerInfo(String playerName) {
        if (INSTANCE.players.containsKey(playerName)) {
            return INSTANCE.players.get(playerName);
        }
        throw new IllegalArgumentException("Player: " + playerName + " was not found.");
    }

    public static synchronized PlayerInfo get(String playerName) {
        return loadPlayerInfo(playerName);
    }

    public static synchronized Set<String> getPlayers() {
        return INSTANCE.players.keySet();
    }

    public static synchronized void remove(String name) {
        INSTANCE.players.remove(name);
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
            Map<String, PlayerInfo> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {
            });
            players.putAll(loaded);
            logger.info("Loaded {} players", players.size());
        } catch (IOException e) {
            logger.error("Unable to load players.", e);
        }
    }
}
