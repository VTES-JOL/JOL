package net.deckserver.storage.json.game;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Data
public class GameTimestampEntry {

    private OffsetDateTime timestamp = OffsetDateTime.now();
    private Map<String, OffsetDateTime> playerTimestamps = new HashMap<>();
    private Map<String, Boolean> playerPings = new HashMap<>();

    public OffsetDateTime getPlayerAccess(String player) {
        return this.playerTimestamps.getOrDefault(player, OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
    }

    public void recordPlayerAccess(String player) {
        this.playerTimestamps.put(player, OffsetDateTime.now());
    }

    public Boolean getPlayerPing(String player) {
        return this.playerPings.getOrDefault(player, false);
    }

    public void setPlayerPing(String player) {
        this.playerPings.put(player, true);
    }

    public void clearPlayerPing(String player) {
        this.playerPings.put(player, false);
    }

    public void removePlayer(String playerName) {
        playerTimestamps.remove(playerName);
        playerPings.remove(playerName);
    }
}
