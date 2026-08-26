package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_activity")
public class PlayerActivityEntity {

    @Id
    @Column(name = "player_id", length = 36)
    private String playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "last_seen", nullable = false)
    private OffsetDateTime lastSeen;

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return player != null ? player.getPlayerName() : null; }

    public OffsetDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(OffsetDateTime lastSeen) { this.lastSeen = lastSeen; }
}
