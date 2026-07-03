package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "jol_game_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_jol_game_history_recorded_at", columnNames = "recorded_at"))
public class GameHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    // started/ended are kept as strings — legacy pastGames.json holds mixed offset formats
    @Column(name = "started", length = 64)
    private String started;

    @Column(name = "ended", length = 64)
    private String ended;

    @Column(name = "results", nullable = false, columnDefinition = "TEXT")
    private String results;

    public Long getId() { return id; }

    public OffsetDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(OffsetDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getStarted() { return started; }
    public void setStarted(String started) { this.started = started; }

    public String getEnded() { return ended; }
    public void setEnded(String ended) { this.ended = ended; }

    public String getResults() { return results; }
    public void setResults(String results) { this.results = results; }
}
