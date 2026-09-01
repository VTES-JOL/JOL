package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "game_chat")
public class GameChatEntity {

    @Id
    @Column(name = "game_id", length = 36)
    private String gameId;

    @Column(name = "history", nullable = false, columnDefinition = "TEXT")
    private String history;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
