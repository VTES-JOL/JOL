package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * One row per game submit that carried a command and/or chat message — the
 * former {@code net.deckserver.metrics} CSV log, now queryable. Fully
 * normalized (no JSON blob column), so unlike most entities here it has no
 * {@code toXxx()}/{@code from()} pair.
 *
 * <p>{@code playerName}/{@code gameName} are denormalized text with no FK:
 * analytics must survive {@code GameCleanUp} deleting games and outlive
 * player rows.
 */
@Entity
@Table(name = "metric_event")
public class MetricEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "player_name", nullable = false, columnDefinition = "TEXT")
    private String playerName;

    @Column(name = "game_name", nullable = false, columnDefinition = "TEXT")
    private String gameName;

    @Column(name = "did_command", nullable = false)
    private boolean didCommand;

    @Column(name = "did_chat", nullable = false)
    private boolean didChat;

    @Column(name = "is_tournament", nullable = false)
    private boolean tournament;

    public MetricEventEntity() {
    }

    public MetricEventEntity(OffsetDateTime occurredAt, String playerName, String gameName,
                             boolean didCommand, boolean didChat, boolean tournament) {
        this.occurredAt = occurredAt;
        this.playerName = playerName;
        this.gameName = gameName;
        this.didCommand = didCommand;
        this.didChat = didChat;
        this.tournament = tournament;
    }

    public Long getId() { return id; }

    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public boolean isDidCommand() { return didCommand; }
    public void setDidCommand(boolean didCommand) { this.didCommand = didCommand; }

    public boolean isDidChat() { return didChat; }
    public void setDidChat(boolean didChat) { this.didChat = didChat; }

    public boolean isTournament() { return tournament; }
    public void setTournament(boolean tournament) { this.tournament = tournament; }
}
