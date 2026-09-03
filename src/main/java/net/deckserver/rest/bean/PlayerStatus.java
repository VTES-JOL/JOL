package net.deckserver.rest.bean;

import lombok.Data;
import net.deckserver.game.model.JolGame;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerGameActivityService;
import net.deckserver.services.PlayerService;

@Data
public class PlayerStatus {
    private String playerName;
    /** Stable, URL-safe id for {@link #playerName} — the path token for that player's deck endpoints. */
    private String playerId;
    private boolean pinged;
    private boolean current;
    /** Live in-memory game state — lets the home Games List show table standing at a glance. */
    private int pool;
    private double vp;
    private boolean ousted;

    public PlayerStatus(String gameName, String playerName) {
        this.playerName = playerName;
        this.playerId = PlayerService.getPlayerId(playerName);
        this.pinged = PlayerGameActivityService.isPlayerPinged(playerName, gameName);
        this.current = PlayerGameActivityService.getPlayerAccess(playerName, gameName).isAfter(PlayerGameActivityService.getGameTimestamp(gameName));
        JolGame game = GameService.getGameByName(gameName);
        this.pool = game.getPool(playerName);
        this.vp = game.getVictoryPoints(playerName);
        this.ousted = game.isOusted(playerName);
    }
}
