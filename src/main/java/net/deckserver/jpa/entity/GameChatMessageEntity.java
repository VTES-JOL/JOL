package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * One row per chat line, replacing the single {@code game_chat.history} JSON blob
 * ({@link GameChatEntity}, retained for one release). Turn grouping is
 * denormalised onto every row; ChatService reconstructs
 * {@code net.deckserver.storage.json.game.TurnData}/{@code TurnHistory} from an
 * ordered list of these.
 */
@Entity
@Table(name = "game_chat_message")
public class GameChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "turn_seq", nullable = false)
    private int turnSeq;

    @Column(name = "chat_seq", nullable = false)
    private int chatSeq;

    @Column(name = "turn_id", nullable = false, length = 50)
    private String turnId;

    @Column(name = "turn_player", nullable = false, length = 255)
    private String turnPlayer;

    @Column(name = "turn_label", nullable = false, length = 320)
    private String turnLabel;

    @Column(name = "posted_at", nullable = false)
    private OffsetDateTime postedAt;

    @Column(name = "display_ts", length = 64)
    private String displayTs;

    @Column(name = "source", length = 320)
    private String source;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "command", columnDefinition = "TEXT")
    private String command;

    @Column(name = "invocation", columnDefinition = "TEXT")
    private String invocation;

    @Column(name = "invocation_by", length = 255)
    private String invocationBy;

    public Long getId() { return id; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public int getTurnSeq() { return turnSeq; }
    public void setTurnSeq(int turnSeq) { this.turnSeq = turnSeq; }

    public int getChatSeq() { return chatSeq; }
    public void setChatSeq(int chatSeq) { this.chatSeq = chatSeq; }

    public String getTurnId() { return turnId; }
    public void setTurnId(String turnId) { this.turnId = turnId; }

    public String getTurnPlayer() { return turnPlayer; }
    public void setTurnPlayer(String turnPlayer) { this.turnPlayer = turnPlayer; }

    public String getTurnLabel() { return turnLabel; }
    public void setTurnLabel(String turnLabel) { this.turnLabel = turnLabel; }

    public OffsetDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(OffsetDateTime postedAt) { this.postedAt = postedAt; }

    public String getDisplayTs() { return displayTs; }
    public void setDisplayTs(String displayTs) { this.displayTs = displayTs; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getInvocation() { return invocation; }
    public void setInvocation(String invocation) { this.invocation = invocation; }

    public String getInvocationBy() { return invocationBy; }
    public void setInvocationBy(String invocationBy) { this.invocationBy = invocationBy; }
}
