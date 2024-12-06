package net.deckserver.dwr.bean;

import lombok.Data;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.JolGame;

@Data
public class PlayerStatus {
    private String playerName;
    private int pool;
    private boolean pinged;
    private boolean current;

    public PlayerStatus(String gameName, String playerName) {
        JolAdmin admin = JolAdmin.INSTANCE;
        this.playerName = playerName;
        JolGame game = admin.getGame(gameName);
        this.pool = game.getPool(playerName);
        this.pinged = admin.isPlayerPinged(playerName, gameName);
        this.current = admin.getPlayerAccess(playerName, gameName).isAfter(admin.getGameTimeStamp(gameName));
    }

    public boolean isOusted() {
        return this.pool == 0;
    }
}
