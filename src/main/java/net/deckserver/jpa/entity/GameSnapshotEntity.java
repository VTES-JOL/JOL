package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "game_snapshot")
public class GameSnapshotEntity {

    @EmbeddedId
    private GameSnapshotId id;

    @Column(name = "state", nullable = false, columnDefinition = "TEXT")
    private String state;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public GameSnapshotEntity() {}

    public GameSnapshotEntity(GameSnapshotId id, String state, OffsetDateTime createdAt) {
        this.id = id;
        this.state = state;
        this.createdAt = createdAt;
    }

    public GameSnapshotId getId() { return id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameSnapshotEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}
