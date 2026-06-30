package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.PlayerActivityRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerActivityService extends PersistedService {

    private static final PlayerActivityRepository playerActivityRepository = new PlayerActivityRepository();
    private static final PlayerActivityService INSTANCE = new PlayerActivityService();

    private final Map<String, OffsetDateTime> playerTimestamps = new ConcurrentHashMap<>();

    private PlayerActivityService() {
        super("PlayerActivityService", 1);
        load();
    }

    public static void recordPlayerAccess(String playerName) {
        if (playerName == null || playerName.isBlank()) return;
        INSTANCE.playerTimestamps.put(playerName, OffsetDateTime.now());
    }

    public static OffsetDateTime getPlayerAccess(String playerName) {
        return INSTANCE.playerTimestamps.getOrDefault(
                playerName,
                OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        );
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
        logger.debug("Persisting {} player timestamps", playerTimestamps.size());
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            playerActivityRepository.saveAll(em, playerTimestamps);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for PlayerActivityService", e);
        }
    }

    @Override
    protected void load() {
        if (testModeEnabled) {
            Path path = DataPaths.path("player-timestamps.json");
            if (!Files.exists(path)) return;
            try {
                Map<String, OffsetDateTime> loaded = objectMapper.readValue(path.toFile(), new TypeReference<>() {});
                playerTimestamps.putAll(loaded);
                logger.info("Loaded {} player timestamps from file", playerTimestamps.size());
            } catch (IOException e) {
                logger.error("Unable to load player timestamps from file", e);
            }
            return;
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            playerActivityRepository.findAll(em).forEach(entity ->
                    playerTimestamps.put(entity.getPlayerName(), entity.getLastSeen()));
            logger.info("Loaded {} player timestamps from JPA", playerTimestamps.size());
        } catch (Exception e) {
            logger.error("JPA load failed for PlayerActivityService", e);
        }
    }
}
