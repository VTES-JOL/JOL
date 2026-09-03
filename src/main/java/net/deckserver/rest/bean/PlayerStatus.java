package net.deckserver.rest.bean;

import lombok.Data;
import net.deckserver.services.PlayerGameActivityService;
import net.deckserver.services.PlayerService;

@Data
public class PlayerStatus {
    private String playerName;
    /** Stable, URL-safe id for {@link #playerName} — the path token for that player's deck endpoints. */
    private String playerId;
    private boolean pinged;
    private boolean current;

    public PlayerStatus(String gameName, String playerName) {
        this.playerName = playerName;
        this.playerId = PlayerService.getPlayerId(playerName);
        this.pinged = PlayerGameActivityService.isPlayerPinged(playerName, gameName);
        this.current = PlayerGameActivityService.getPlayerAccess(playerName, gameName).isAfter(PlayerGameActivityService.getGameTimestamp(gameName));
    }
}
