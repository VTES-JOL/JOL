package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeckInfoId implements Serializable {

    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    @Column(name = "deck_name", nullable = false)
    private String deckName;

    public DeckInfoId() {}

    public DeckInfoId(String playerId, String deckName) {
        this.playerId = playerId;
        this.deckName = deckName;
    }

    public String getPlayerId() { return playerId; }
    public String getDeckName() { return deckName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckInfoId that)) return false;
        return Objects.equals(playerId, that.playerId) && Objects.equals(deckName, that.deckName);
    }

    @Override
    public int hashCode() { return Objects.hash(playerId, deckName); }
}
