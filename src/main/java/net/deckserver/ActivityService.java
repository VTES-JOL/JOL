package net.deckserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.storage.json.game.GameTimestampEntry;
import net.deckserver.storage.json.game.Timestamps;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Map;

public class ActivityService {

    private static final String basePath = System.getenv("JOL_DATA");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Timestamps timestamps;

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        Path filePath = Paths.get(basePath, "timestamps.json");
        try {
            timestamps = objectMapper.readValue(filePath.toFile(), Timestamps.class);
        } catch (IOException e) {
            timestamps = new Timestamps();
        }
    }

    public static void setGameTimestamp(String gameName) {
        timestamps.setGameTimestamp(gameName);
    }

    public static void recordPlayerAccess(String playerName) {
        timestamps.recordPlayerAccess(playerName);
    }

    public static OffsetDateTime getGameTimestamp(String gameName) {
        return timestamps.getGameTimestamp(gameName);
    }

    public static OffsetDateTime getPlayerAccess(String playerName) {
        return timestamps.getPlayerAccess(playerName);
    }

    public static void recordPlayerAccess(String playerName, String gameName) {
        timestamps.recordPlayerAccess(playerName, gameName);
    }

    public static OffsetDateTime getPlayerAccess(String playerName, String gameName) {
        return timestamps.getPlayerAccess(playerName, gameName);
    }

    public static boolean isPlayerPinged(String playerName, String gameName) {
        return timestamps.isPlayerPinged(playerName, gameName);
    }

    public static void pingPlayer(String playerName, String gameName) {
        timestamps.pingPlayer(playerName, gameName);
    }

    public static void clearPing(String playerName, String gameName) {
        timestamps.clearPing(playerName, gameName);
    }

    public static void clearGame(String gameName) {
        timestamps.clearGame(gameName);
    }

    public static Map<String, GameTimestampEntry> getGameTimestamps() {
        return timestamps.getGameTimestamps();
    }
}
