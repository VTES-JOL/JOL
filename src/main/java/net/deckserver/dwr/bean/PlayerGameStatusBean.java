package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.JolGame;

import java.time.OffsetDateTime;

public class PlayerGameStatusBean {

    private final String gameName;
    private final String gameId;
    private final String playerName;
    private final int pool;
    private final boolean pinged;
    private final boolean flagged;
    private final boolean ousted;
    private final boolean current;
    private final boolean turn;
    public PlayerGameStatusBean(String gameName, String playerName) {
        JolAdmin admin = JolAdmin.INSTANCE;
        JolGame game = admin.getGame(gameName);
        this.gameId = admin.getGameId(gameName);
        this.gameName = gameName;
        this.playerName = playerName;
        this.pool = game.getPool(playerName);
        this.pinged = admin.isPlayerPinged(playerName, gameName);
        this.flagged = this.pool < 0;
        this.ousted = this.pool == 0;
        OffsetDateTime access = admin.getPlayerAccess(playerName, gameName);
        OffsetDateTime timestamp = admin.getGameTimeStamp(gameName);
        this.current = timestamp.isBefore(access);
        this.turn = playerName.equals(game.getActivePlayer());
    }

    public String getGameId() { return  gameId; }
    public String getGameName() {
        return gameName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPool() {
        return pool;
    }

    public boolean isPinged() {
        return pinged;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public boolean isOusted() {
        return ousted;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isTurn() {
        return turn;
    }
}
