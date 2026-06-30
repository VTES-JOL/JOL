package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameActivityRepository;
import net.deckserver.storage.json.game.GameTimestampEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerGameActivityService extends PersistedService {

    private static final Path PERSISTENCE_PATH = DataPaths.path("game-timestamps.json");
    private static final GameActivityRepository gameActivityRepository = new GameActivityRepository();
    private static final PlayerGameActivityService INSTANCE = new PlayerGameActivityService();

    private final Map<String, GameTimestampEntry> gameTimestamps = new ConcurrentHashMap<>();

    private PlayerGameActivityService() {
        super("PlayerGameActivityService", 1); // 1 minute persistence interval
        load(); // Load existing data on startup
    }

    public static  void recordPlayerAccess(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        getOrCreateGameTimestampEntry(gameName).recordPlayerAccess(playerName);
    }

    public static  OffsetDateTime getPlayerAccess(String playerName, String gameName) {
        GameTimestampEntry e = getExistingGameTimestampEntry(gameName);
        return e != null ? e.getPlayerAccess(playerName) : OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    public static  boolean isPlayerPinged(String playerName, String gameName) {
        GameTimestampEntry e = getExistingGameTimestampEntry(gameName);
        return e != null && e.getPlayerPing(playerName);
    }

    public static  void pingPlayer(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        getOrCreateGameTimestampEntry(gameName).setPlayerPing(playerName);
    }

    public static  void clearPing(String playerName, String gameName) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        getOrCreateGameTimestampEntry(gameName).clearPlayerPing(playerName);
    }

    public static   void clearGame(String gameName) {
        if (gameName == null || gameName.isBlank()) return;
        INSTANCE.gameTimestamps.remove(gameName);
        INSTANCE.jpaWrite(em -> gameActivityRepository.delete(em, gameName));
    }

    public static  OffsetDateTime getGameTimestamp(String game) {
        GameTimestampEntry e = getExistingGameTimestampEntry(game);
        return e != null ? e.getTimestamp() : OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    public static  Map<String, GameTimestampEntry> getGameTimestamps() {
        return INSTANCE.gameTimestamps;
    }

    public static  void setGameTimestamp(String game) {
        if (game == null || game.isBlank()) return;
        getOrCreateGameTimestampEntry(game).setTimestamp(OffsetDateTime.now());
    }

    public static  boolean isCurrent(String player, String game) {
        OffsetDateTime playerAccess = getPlayerAccess(player, game);
        OffsetDateTime gameLastUpdated = getGameTimestamp(game);
        return playerAccess.isAfter(gameLastUpdated);
    }


    private static  GameTimestampEntry getExistingGameTimestampEntry(String game) {
        if (game == null || game.isBlank()) return null;
        return INSTANCE.gameTimestamps.get(game);
    }

    private static  GameTimestampEntry getOrCreateGameTimestampEntry(String game) {
        if (game == null || game.isBlank()) return null;
        GameTimestampEntry gameTimestampEntry = INSTANCE.gameTimestamps.get(game);
        if (gameTimestampEntry == null) {
            gameTimestampEntry = new GameTimestampEntry();
            INSTANCE.gameTimestamps.put(game, gameTimestampEntry);
        }
        return gameTimestampEntry;
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
            logger.debug("Persisting {} game timestamps", gameTimestamps.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), gameTimestamps);
            logger.debug("Successfully persisted game timestamps");
        } catch (IOException e) {
            logger.error("Unable to save game timestamps", e);
        }
        jpaWrite(em -> gameActivityRepository.saveAll(em, gameTimestamps));
    }

    private void jpaWrite(java.util.function.Consumer<EntityManager> action) {
        if (!JpaFactory.isEnabled()) return;
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for PlayerGameActivityService", e);
        }
    }

    @Override
    protected void load() {
        if (JpaFactory.isEnabled()) {
            try (EntityManager em = JpaFactory.createEntityManager()) {
                gameTimestamps.putAll(gameActivityRepository.findAllAsMap(em));
                logger.info("Loaded {} game timestamps from JPA", gameTimestamps.size());
            } catch (Exception e) {
                logger.error("JPA load failed for PlayerGameActivityService", e);
            }
            return;
        }
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing game timestamps file found");
            return;
        }
        try {
            Map<String, GameTimestampEntry> loaded = objectMapper.readValue(
                    PERSISTENCE_PATH.toFile(), new TypeReference<>() {});
            gameTimestamps.putAll(loaded);
            logger.info("Loaded {} game timestamps", gameTimestamps.size());
        } catch (IOException e) {
            logger.error("Unable to load game timestamps", e);
        }
    }
}
