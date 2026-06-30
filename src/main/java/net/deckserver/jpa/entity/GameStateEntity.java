package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "jol_game_state")
public class GameStateEntity {

    @Id
    @Column(name = "game_id", length = 36)
    private String gameId;

    @Column(name = "state", nullable = false, columnDefinition = "TEXT")
    private String state;

    @Column(name = "current_player")
    private String currentPlayer;

    @Column(name = "turn", length = 50)
    private String turn;

    @Column(name = "phase", length = 50)
    private String phase;

    @Column(name = "player_count")
    private int playerCount;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version")
    private long version;

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public String getTurn() { return turn; }
    public void setTurn(String turn) { this.turn = turn; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getEntityVersion() { return version; }
}
