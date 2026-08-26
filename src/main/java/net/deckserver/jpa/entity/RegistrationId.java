package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RegistrationId implements Serializable {

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    public RegistrationId() {}

    public RegistrationId(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public String getGameId() { return gameId; }
    public String getPlayerId() { return playerId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationId that)) return false;
        return Objects.equals(gameId, that.gameId) && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() { return Objects.hash(gameId, playerId); }
}
