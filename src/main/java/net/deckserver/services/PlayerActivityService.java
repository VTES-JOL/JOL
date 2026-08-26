package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.PlayerActivityRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Startup
public class PlayerActivityService extends PersistedService {

    private static final PlayerActivityRepository playerActivityRepository = new PlayerActivityRepository();
    private static PlayerActivityService instance() {
        return resolve(PlayerActivityService.class, PlayerActivityService::new);
    }

    private final Map<String, OffsetDateTime> playerTimestamps = new ConcurrentHashMap<>();

    PlayerActivityService() {
        super("PlayerActivityService", 1); // 1 minute persistence interval
    }

    // Called on essentially every request (e.g. the nav poll every logged-in tab runs
    // continuously) - kept as an in-memory write with a batched flush rather than a
    // per-call JPA write-through, which would put every poll on the DB's write path.
    public static  void recordPlayerAccess(String playerName) {
        if (playerName == null || playerName.isBlank()) return;
        instance().playerTimestamps.put(playerName, OffsetDateTime.now());
    }

    public static  OffsetDateTime getPlayerAccess(String playerName) {
        return instance().playerTimestamps.getOrDefault(
                playerName,
                OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        );
    }

    public static PersistedService getInstance() {
        return instance();
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        logger.debug("Persisting {} player timestamps", playerTimestamps.size());
        requireJpaWrite(em -> playerActivityRepository.saveAll(em, playerTimestamps));
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            playerActivityRepository.findAll(em).forEach(entity ->
                    playerTimestamps.put(entity.getPlayerName(), entity.getLastSeen()));
            logger.info("Loaded {} player timestamps from JPA", playerTimestamps.size());
        } catch (Exception e) {
            logger.error("JPA load failed for PlayerActivityService", e);
        }
    }
}
