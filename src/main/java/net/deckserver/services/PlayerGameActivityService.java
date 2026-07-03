package net.deckserver.services;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameActivityRepository;
import net.deckserver.storage.json.game.GameTimestampEntry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerGameActivityService extends PersistedService {

    private static final GameActivityRepository gameActivityRepository = new GameActivityRepository();
    private static final PlayerGameActivityService INSTANCE = new PlayerGameActivityService();

    private final Map<String, GameTimestampEntry> gameTimestamps = new ConcurrentHashMap<>();

    private PlayerGameActivityService() {
        super("PlayerGameActivityService", 1);
        load();
    }

    public static void recordPlayerAccess(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        getOrCreateGameTimestampEntry(gameName).recordPlayerAccess(playerName);
    }

    public static OffsetDateTime getPlayerAccess(String playerName, String gameName) {
        GameTimestampEntry e = getExistingGameTimestampEntry(gameName);
        return e != null ? e.getPlayerAccess(playerName) : OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    public static boolean isPlayerPinged(String playerName, String gameName) {
        GameTimestampEntry e = getExistingGameTimestampEntry(gameName);
        return e != null && e.getPlayerPing(playerName);
    }

    public static void pingPlayer(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        getOrCreateGameTimestampEntry(gameName).setPlayerPing(playerName);
    }

    public static void clearPing(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        getOrCreateGameTimestampEntry(gameName).clearPlayerPing(playerName);
    }

    public static void clearGame(String gameName) {
        if (gameName == null || gameName.isBlank()) return;
        INSTANCE.gameTimestamps.remove(gameName);
        INSTANCE.jpaWrite(em -> gameActivityRepository.delete(em, gameName));
    }

    public static OffsetDateTime getGameTimestamp(String game) {
        GameTimestampEntry e = getExistingGameTimestampEntry(game);
        return e != null ? e.getTimestamp() : OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    public static Map<String, GameTimestampEntry> getGameTimestamps() {
        return INSTANCE.gameTimestamps;
    }

    public static void setGameTimestamp(String game) {
        if (game == null || game.isBlank()) return;
        getOrCreateGameTimestampEntry(game).setTimestamp(OffsetDateTime.now());
    }

    public static boolean isCurrent(String player, String game) {
        OffsetDateTime playerAccess = getPlayerAccess(player, game);
        OffsetDateTime gameLastUpdated = getGameTimestamp(game);
        return playerAccess.isAfter(gameLastUpdated);
    }

    private static GameTimestampEntry getExistingGameTimestampEntry(String game) {
        if (game == null || game.isBlank()) return null;
        return INSTANCE.gameTimestamps.get(game);
    }

    private static GameTimestampEntry getOrCreateGameTimestampEntry(String game) {
        if (game == null || game.isBlank()) return null;
        return INSTANCE.gameTimestamps.computeIfAbsent(game, k -> new GameTimestampEntry());
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
        logger.debug("Persisting {} game timestamps", gameTimestamps.size());
        jpaWrite(em -> gameActivityRepository.saveAll(em, gameTimestamps));
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            gameTimestamps.putAll(gameActivityRepository.findAllAsMap(em));
            logger.info("Loaded {} game timestamps from JPA", gameTimestamps.size());
        } catch (Exception e) {
            logger.error("JPA load failed for PlayerGameActivityService", e);
        }
    }
}
