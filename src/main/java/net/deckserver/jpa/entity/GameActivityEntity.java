package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "game_activity")
public class GameActivityEntity {

    @Id
    @Column(name = "game_id", length = 36)
    private String gameId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", referencedColumnName = "game_id", insertable = false, updatable = false)
    private GameInfoEntity game;

    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    @Column(name = "player_timestamps", nullable = false, columnDefinition = "TEXT")
    private String playerTimestamps = "{}";

    @Column(name = "player_pings", nullable = false, columnDefinition = "TEXT")
    private String playerPings = "{}";

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getGameName() { return game != null ? game.getGameName() : null; }

    public OffsetDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(OffsetDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getPlayerTimestamps() { return playerTimestamps; }
    public void setPlayerTimestamps(String playerTimestamps) { this.playerTimestamps = playerTimestamps; }

    public String getPlayerPings() { return playerPings; }
    public void setPlayerPings(String playerPings) { this.playerPings = playerPings; }
}
