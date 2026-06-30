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

    private static final Path PERSISTENCE_PATH = DataPaths.path("player-timestamps.json");
    private static final PlayerActivityRepository playerActivityRepository = new PlayerActivityRepository();
    private static final PlayerActivityService INSTANCE = new PlayerActivityService();

    private final Map<String, OffsetDateTime> playerTimestamps = new ConcurrentHashMap<>();

    private PlayerActivityService() {
        super("PlayerActivityService", 1); // 1 minute persistence interval
        load(); // Load existing data on startup
    }

    public static  void recordPlayerAccess(String playerName) {
        if (playerName == null || playerName.isBlank()) return;
        INSTANCE.playerTimestamps.put(playerName, OffsetDateTime.now());
    }

    public static  OffsetDateTime getPlayerAccess(String playerName) {
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

        try {
            logger.debug("Persisting {} player timestamps", playerTimestamps.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), playerTimestamps);
            logger.debug("Successfully persisted player timestamps");
        } catch (IOException e) {
            logger.error("Unable to save player timestamps", e);
        }
        jpaWrite(em -> playerActivityRepository.saveAll(em, playerTimestamps));
    }

    private void jpaWrite(java.util.function.Consumer<EntityManager> action) {
        if (!JpaFactory.isEnabled()) return;
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for PlayerActivityService", e);
        }
    }

    @Override
    protected void load() {
        if (JpaFactory.isEnabled()) {
            try (EntityManager em = JpaFactory.createEntityManager()) {
                playerActivityRepository.findAll(em).forEach(entity ->
                        playerTimestamps.put(entity.getPlayerName(), entity.getLastSeen()));
                logger.info("Loaded {} player timestamps from JPA", playerTimestamps.size());
            } catch (Exception e) {
                logger.error("JPA load failed for PlayerActivityService", e);
            }
            return;
        }
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing player timestamps file found");
            return;
        }
        try {
            Map<String, OffsetDateTime> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {});
            playerTimestamps.putAll(loaded);
            logger.info("Loaded {} player timestamps", playerTimestamps.size());
        } catch (IOException e) {
            logger.error("Unable to load player timestamps", e);
        }
    }
}