package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GameSnapshotId implements Serializable {

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "turn", nullable = false, length = 32)
    private String turn;

    public GameSnapshotId() {}

    public GameSnapshotId(String gameId, String turn) {
        this.gameId = gameId;
        this.turn = turn;
    }

    public String getGameId() { return gameId; }
    public String getTurn() { return turn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameSnapshotId that)) return false;
        return Objects.equals(gameId, that.gameId) && Objects.equals(turn, that.turn);
    }

    @Override
    public int hashCode() { return Objects.hash(gameId, turn); }
}
