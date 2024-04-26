package net.deckserver.storage.json.game;

import lombok.Data;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

@Data
public class Timestamps {

    private static final Logger logger = getLogger(Timestamps.class);

    private Map<String, OffsetDateTime> playerTimestamps = new HashMap<>();
    private Map<String, GameTimestampEntry> gameTimestamps = new HashMap<>();

    public void clearGame(String gameName) {
        this.gameTimestamps.remove(gameName);
    }

    public void recordPlayerAccess(String player) {
        this.playerTimestamps.put(player, OffsetDateTime.now());
    }

    public OffsetDateTime getPlayerAccess(String player) {
        return this.playerTimestamps.getOrDefault(player, OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
    }

    private GameTimestampEntry getGameTimestampEntry(String game) {
        GameTimestampEntry gameTimestampEntry = this.gameTimestamps.get(game);
        if (gameTimestampEntry == null) {
            gameTimestampEntry = new GameTimestampEntry();
            this.gameTimestamps.put(game, gameTimestampEntry);
        }
        return gameTimestampEntry;
    }

    public OffsetDateTime getGameTimestamp(String game) {
        return getGameTimestampEntry(game).getTimestamp();
    }

    public void setGameTimestamp(String game) {
        logger.debug("Game {} state modified at {}", game, OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        getGameTimestampEntry(game).setTimestamp(OffsetDateTime.now());
    }

    public OffsetDateTime getPlayerAccess(String player, String game) {
        return getGameTimestampEntry(game).getPlayerAccess(player);
    }

    public void recordPlayerAccess(String player, String game) {
        logger.debug("{} entering game {} at {}", player, game, OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        getGameTimestampEntry(game).recordPlayerAccess(player);
    }

    public boolean isPlayerPinged(String player, String game) {
        return getGameTimestampEntry(game).getPlayerPing(player);
    }

    public void clearPing(String player, String game) {
        logger.debug("Clearing {} ping in game {}", player, game);
        getGameTimestampEntry(game).clearPlayerPing(player);
    }

    public void pingPlayer(String player, String game) {
        logger.debug("{} being pinged in game {}", player, game);
        getGameTimestampEntry(game).setPlayerPing(player);
    }
}
