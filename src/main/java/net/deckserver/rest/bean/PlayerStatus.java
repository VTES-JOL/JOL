package net.deckserver.rest.bean;

import lombok.Data;
import net.deckserver.services.PlayerGameActivityService;

@Data
public class PlayerStatus {
    private String playerName;
    private boolean pinged;
    private boolean current;

    public PlayerStatus(String gameName, String playerName) {
        this.playerName = playerName;
        this.pinged = PlayerGameActivityService.isPlayerPinged(playerName, gameName);
        this.current = PlayerGameActivityService.getPlayerAccess(playerName, gameName).isAfter(PlayerGameActivityService.getGameTimestamp(gameName));
    }
}
