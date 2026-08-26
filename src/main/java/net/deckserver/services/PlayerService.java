package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Streams;
import io.azam.ulidj.ULID;
import jakarta.persistence.EntityManager;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.PlayerRepository;
import net.deckserver.storage.json.system.PlayerInfo;
import net.deckserver.storage.json.system.UserSummary;
import org.mindrot.jbcrypt.BCrypt;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
@Startup
public class PlayerService extends PersistedService {

    private static final PlayerRepository playerRepository = new PlayerRepository();
    private static PlayerService instance() {
        return resolve(PlayerService.class, PlayerService::new);
    }
    private static final LoadingCache<String, UserSummary> activeUsers = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .refreshAfterWrite(1, TimeUnit.MINUTES)
            .build(PlayerService::generateSummary);
    private static final Predicate<UserSummary> RECENTLY_ONLINE = summary -> OffsetDateTime.parse(summary.getLastOnline()).plusMinutes(30).isAfter(OffsetDateTime.now());
    private final Map<String, PlayerInfo> players = new ConcurrentHashMap<>();

    PlayerService() {
        super("PlayerService", 0);
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

    public static boolean existsPlayer(String name) {
        return name != null && instance().players.containsKey(name);
    }

    public static boolean registerPlayer(String name, String password, String email) {
        if (existsPlayer(name) || name.isEmpty())
            return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        PlayerInfo player = new PlayerInfo(name, ULID.random(), email, hash);
        return instance().jpaWriteThenMutate(
                em -> playerRepository.save(em, player),
                () -> instance().players.put(name, player));
    }

    public static boolean authenticate(String playerName, String password) {
        if (existsPlayer(playerName)) {
            PlayerInfo playerInfo = loadPlayerInfo(playerName);
            String hash = playerInfo.getHash();
            return hash != null && BCrypt.checkpw(password, hash);
        } else {
            return false;
        }
    }

    public static void changePassword(String player, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        PlayerInfo info = loadPlayerInfo(player);
        String previousHash = info.getHash();
        instance().jpaWriteWithRollback(
                () -> info.setHash(hash),
                em -> playerRepository.save(em, info),
                () -> info.setHash(previousHash));
    }

    public static void updateProfile(String playerName, String email, String discordID, String veknID, String country) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        String previousDiscordId = playerInfo.getDiscordId();
        String previousEmail = playerInfo.getEmail();
        String previousVeknId = playerInfo.getVeknId();
        String previousCountryCode = playerInfo.getCountryCode();
        if (instance().jpaWriteWithRollback(
                () -> {
                    playerInfo.setDiscordId(discordID);
                    playerInfo.setEmail(email);
                    playerInfo.setVeknId(veknID);
                    playerInfo.setCountryCode(country);
                },
                em -> playerRepository.save(em, playerInfo),
                () -> {
                    playerInfo.setDiscordId(previousDiscordId);
                    playerInfo.setEmail(previousEmail);
                    playerInfo.setVeknId(previousVeknId);
                    playerInfo.setCountryCode(previousCountryCode);
                })) {
            refreshActive(playerName);
        }
    }

    public static void setImageTooltipPreference(String playerName, boolean value) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        boolean previousValue = playerInfo.isShowImages();
        instance().jpaWriteWithRollback(
                () -> playerInfo.setShowImages(value),
                em -> playerRepository.save(em, playerInfo),
                () -> playerInfo.setShowImages(previousValue));
    }

    public static void setEdgeColor(String playerName, String value) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        String previousValue = playerInfo.getEdgeColor();
        instance().jpaWriteWithRollback(
                () -> playerInfo.setEdgeColor(value),
                em -> playerRepository.save(em, playerInfo),
                () -> playerInfo.setEdgeColor(previousValue));
    }

    public static void setNotificationPreference(String playerName, boolean value) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        boolean previousValue = playerInfo.isNotificationsEnabled();
        instance().jpaWriteWithRollback(
                () -> playerInfo.setNotificationsEnabled(value),
                em -> playerRepository.save(em, playerInfo),
                () -> playerInfo.setNotificationsEnabled(previousValue));
    }

    public static void setRole(String playerName, PlayerRole role, boolean enabled) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        Set<PlayerRole> previousRoles = new HashSet<>(playerInfo.getRoles());
        instance().jpaWriteWithRollback(
                () -> {
                    if (enabled) {
                        playerInfo.getRoles().add(role);
                    } else {
                        playerInfo.getRoles().remove(role);
                    }
                },
                em -> playerRepository.save(em, playerInfo),
                () -> {
                    playerInfo.getRoles().clear();
                    playerInfo.getRoles().addAll(previousRoles);
                });
    }

    private static PlayerInfo loadPlayerInfo(String playerName) {
        if (instance().players.containsKey(playerName)) {
            return instance().players.get(playerName);
        }
        throw new IllegalArgumentException("Player: " + playerName + " was not found.");
    }

    public static PlayerInfo get(String playerName) {
        return loadPlayerInfo(playerName);
    }

    public static Set<String> getPlayers() {
        return instance().players.keySet();
    }

    public static void remove(String name) {
        instance().jpaWriteThenMutate(
                em -> playerRepository.delete(em, name),
                () -> instance().players.remove(name));
    }

    public static PersistedService getInstance() {
        return instance();
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background file flush needed
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            Map<String, PlayerInfo> loaded = playerRepository.findAll(em);
            players.putAll(loaded);
            logger.info("Loaded {} players from JPA", players.size());
        } catch (Exception e) {
            logger.error("JPA load failed for PlayerService", e);
        }
    }
}
