package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.JolGame;

public class PlayerGameStatusBean {

    private final String gameName;
    private final String playerName;
    private final int pool;
    public PlayerGameStatusBean(String gameName, String playerName) {
        JolGame game = JolAdmin.getInstance().getGame(gameName);
        this.gameName = gameName;
        this.playerName = playerName;
        this.pool = game.getPool(playerName);
    }

    public String getGameName() {
        return gameName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPool() {
        return pool;
    }
}
