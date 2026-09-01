package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * A player command that failed to parse or validate. Deliberately separate from
 * {@link GameChatMessageEntity}: these never enter {@code TurnHistory} or the
 * normal chat render, and are served only to a judge (see ChatService /
 * GameActionResource).
 */
@Entity
@Table(name = "game_command_error")
public class GameCommandErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "turn_label", nullable = false, length = 320)
    private String turnLabel;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "display_ts", length = 64)
    private String displayTs;

    @Column(name = "player", nullable = false, length = 255)
    private String player;

    @Column(name = "raw_command", nullable = false, columnDefinition = "TEXT")
    private String rawCommand;

    @Column(name = "error_text", columnDefinition = "TEXT")
    private String errorText;

    public Long getId() { return id; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getTurnLabel() { return turnLabel; }
    public void setTurnLabel(String turnLabel) { this.turnLabel = turnLabel; }

    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getDisplayTs() { return displayTs; }
    public void setDisplayTs(String displayTs) { this.displayTs = displayTs; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public String getRawCommand() { return rawCommand; }
    public void setRawCommand(String rawCommand) { this.rawCommand = rawCommand; }

    public String getErrorText() { return errorText; }
    public void setErrorText(String errorText) { this.errorText = errorText; }
}
