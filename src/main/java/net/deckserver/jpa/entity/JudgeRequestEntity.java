package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.game.enums.JudgeRequestCategory;
import net.deckserver.game.enums.JudgeRequestStatus;

import java.time.OffsetDateTime;

/**
 * A "call a judge" request raised from inside a game. One OPEN row per game at a
 * time (partial unique index in V21). Rows outlive the game itself:
 * {@code game_id} is {@code ON DELETE SET NULL} and {@link #gameName} /
 * {@link #tournamentName} are denormalised snapshots, so the ruling history
 * stays readable after game cleanup.
 */
@Entity
@Table(name = "judge_request")
public class JudgeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "game_id", length = 36)
    private String gameId;

    @Column(name = "game_name", nullable = false, length = 255)
    private String gameName;

    @Column(name = "tournament_name", length = 255)
    private String tournamentName;

    @Column(name = "requested_by", nullable = false, length = 255)
    private String requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 24)
    private JudgeRequestCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private JudgeRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "raw_details", nullable = false, columnDefinition = "TEXT")
    private String rawDetails;

    @Column(name = "parsed_details", nullable = false, columnDefinition = "TEXT")
    private String parsedDetails;

    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "resolution_raw", columnDefinition = "TEXT")
    private String resolutionRaw;

    @Column(name = "resolution_parsed", columnDefinition = "TEXT")
    private String resolutionParsed;

    public Long getId() { return id; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public JudgeRequestCategory getCategory() { return category; }
    public void setCategory(JudgeRequestCategory category) { this.category = category; }

    public JudgeRequestStatus getStatus() { return status; }
    public void setStatus(JudgeRequestStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRawDetails() { return rawDetails; }
    public void setRawDetails(String rawDetails) { this.rawDetails = rawDetails; }

    public String getParsedDetails() { return parsedDetails; }
    public void setParsedDetails(String parsedDetails) { this.parsedDetails = parsedDetails; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionRaw() { return resolutionRaw; }
    public void setResolutionRaw(String resolutionRaw) { this.resolutionRaw = resolutionRaw; }

    public String getResolutionParsed() { return resolutionParsed; }
    public void setResolutionParsed(String resolutionParsed) { this.resolutionParsed = resolutionParsed; }
}
