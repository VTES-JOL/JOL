package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

@Data
public class Timestamps {

    private static final Logger logger = getLogger(Timestamps.class);

    private Map<String, OffsetDateTime> playerTimestamps = new HashMap<>();
    private Map<String, GameTimestampEntry> gameTimestamps = new HashMap<>();

    public void clearGame(String gameName) {
        if (gameName == null || gameName.isBlank()) return;
        this.gameTimestamps.remove(gameName);
    }

    public void recordPlayerAccess(String player) {
        if (player == null || player.isBlank()) return;
        this.playerTimestamps.put(player, OffsetDateTime.now());
    }

    public OffsetDateTime getPlayerAccess(String player) {
        return this.playerTimestamps.getOrDefault(player, OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
    }

    @JsonIgnore
    public Set<String> getPlayers() {
        return this.playerTimestamps.keySet();
    }

    public void removePlayer(String playerName) {
        if (playerName == null || playerName.isBlank()) return;
        playerTimestamps.remove(playerName);
        gameTimestamps.forEach((gameName, entry) -> entry.removePlayer(playerName));
    }

    public OffsetDateTime getGameTimestamp(String game) {
        GameTimestampEntry e = getExistingGameTimestampEntry(game);
        return e != null ? e.getTimestamp() : OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    public void setGameTimestamp(String game) {
        if (game == null || game.isBlank()) return;
        logger.debug("Game {} state modified at {}", game, OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        getOrCreateGameTimestampEntry(game).setTimestamp(OffsetDateTime.now());
    }

    public OffsetDateTime getPlayerAccess(String player, String game) {
        GameTimestampEntry e = getExistingGameTimestampEntry(game);
        return e != null ? e.getPlayerAccess(player) : OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    public void recordPlayerAccess(String player, String game) {
        if (player == null || player.isBlank() || game == null || game.isBlank()) return;
        logger.debug("{} entering game {} at {}", player, game, OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        getOrCreateGameTimestampEntry(game).recordPlayerAccess(player);
    }

    public boolean isPlayerPinged(String player, String game) {
        GameTimestampEntry e = getExistingGameTimestampEntry(game);
        return e != null && e.getPlayerPing(player);
    }

    public void clearPing(String player, String game) {
        if (player == null || player.isBlank() || game == null || game.isBlank()) return;
        logger.debug("Clearing {} ping in game {}", player, game);
        getOrCreateGameTimestampEntry(game).clearPlayerPing(player);
    }

    public void pingPlayer(String player, String game) {
        if (player == null || player.isBlank() || game == null || game.isBlank()) return;
        logger.debug("{} being pinged in game {}", player, game);
        getOrCreateGameTimestampEntry(game).setPlayerPing(player);
    }

    private GameTimestampEntry getExistingGameTimestampEntry(String game) {
        if (game == null || game.isBlank()) return null;
        return this.gameTimestamps.get(game);
    }

    private GameTimestampEntry getOrCreateGameTimestampEntry(String game) {
        if (game == null || game.isBlank()) return null;
        GameTimestampEntry gameTimestampEntry = this.gameTimestamps.get(game);
        if (gameTimestampEntry == null) {
            gameTimestampEntry = new GameTimestampEntry();
            this.gameTimestamps.put(game, gameTimestampEntry);
        }
        return gameTimestampEntry;
    }
}
