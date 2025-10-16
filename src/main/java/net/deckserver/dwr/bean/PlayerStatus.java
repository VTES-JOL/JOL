package net.deckserver.dwr.bean;

import lombok.Data;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.services.PlayerActivityService;
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
