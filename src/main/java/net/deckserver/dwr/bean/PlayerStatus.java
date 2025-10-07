package net.deckserver.dwr.bean;

import lombok.Data;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.JolGame;

@Data
public class PlayerStatus {
    private String playerName;
    private int pool;
    private boolean pinged;
    private boolean current;

    public PlayerStatus(String gameName, String playerName) {
        this.playerName = playerName;
        JolGame game = JolAdmin.getGame(gameName);
        this.pool = game.getPool(playerName);
        this.pinged = JolAdmin.isPlayerPinged(playerName, gameName);
        this.current = JolAdmin.getPlayerAccess(playerName, gameName).isAfter(JolAdmin.getGameTimeStamp(gameName));
    }

    public boolean isOusted() {
        return this.pool == 0;
    }
}
