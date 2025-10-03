package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerActivityService extends PersistedService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "player-timestamps.json");
    private static final PlayerActivityService INSTANCE = new PlayerActivityService();


    private final Map<String, OffsetDateTime> playerTimestamps = new ConcurrentHashMap<>();

    private PlayerActivityService() {
        super("PlayerActivityService", 1); // 1 minute persistence interval
        load(); // Load existing data on startup
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
    }

    @Override
    protected void load() {
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