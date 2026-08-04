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
        super("PlayerGameActivityService", 0);
        load();
    }

    public static void recordPlayerAccess(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        updateGameTimestampEntry(gameName, entry -> entry.recordPlayerAccess(playerName));
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
        updateGameTimestampEntry(gameName, entry -> entry.setPlayerPing(playerName));
    }

    public static void clearPing(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        updateGameTimestampEntry(gameName, entry -> entry.clearPlayerPing(playerName));
    }

    public static void clearGame(String gameName) {
        if (gameName == null || gameName.isBlank()) return;
        INSTANCE.jpaWriteThenMutate(
                em -> gameActivityRepository.delete(em, gameName),
                () -> INSTANCE.gameTimestamps.remove(gameName));
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
        updateGameTimestampEntry(game, entry -> entry.setTimestamp(OffsetDateTime.now()));
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

    private static void updateGameTimestampEntry(String game, java.util.function.Consumer<GameTimestampEntry> mutation) {
        GameTimestampEntry updated = copyOf(INSTANCE.gameTimestamps.get(game));
        mutation.accept(updated);
        INSTANCE.jpaWriteThenMutate(
                em -> gameActivityRepository.save(em, game, updated),
                () -> INSTANCE.gameTimestamps.put(game, updated));
    }

    private static GameTimestampEntry copyOf(GameTimestampEntry source) {
        GameTimestampEntry copy = new GameTimestampEntry();
        if (source != null) {
            copy.setTimestamp(source.getTimestamp());
            copy.setPlayerTimestamps(new ConcurrentHashMap<>(source.getPlayerTimestamps()));
            copy.setPlayerPings(new ConcurrentHashMap<>(source.getPlayerPings()));
        }
        return copy;
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
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
