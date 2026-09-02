package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ChatData {

    private static final DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");

    private String timestamp;
    private String message;
    private String source;
    private String command;
    /** Raw text the player submitted for the command that produced this line. Judge-only; stripped for other viewers. */
    private String invocation;
    /** Who issued that command — often not {@link #source}, which is frequently the affected player. Judge-only. */
    private String invocationBy;
    /**
     * Monotonic id identifying the single command submission that produced this line —
     * shared by every line that submission emitted, distinct for the next one even
     * when {@link #invocation} is byte-identical. Lets judges' chat log show the
     * "» command" header once per submission rather than once per distinct text.
     * Judge-only; stripped for other viewers.
     */
    private Long invocationSeq;
    /**
     * Full-precision ISO-8601 insert time ({@code game_chat_message.posted_at}), as
     * opposed to the minute-granularity display string in {@link #timestamp}. Lets
     * the judges' chat log interleave failed command attempts
     * ({@link CommandErrorData#getOccurredAt()}) at their true chronological position
     * rather than rounding to the minute.
     */
    private String postedAt;

    public ChatData() {
    }

    public ChatData(String message, String source, String command) {
        this.timestamp = OffsetDateTime.now().format(SIMPLE_FORMAT);
        this.message = message;
        this.source = source;
        this.command = command;
    }

    public ChatData(OffsetDateTime timestamp, String message, String source) {
        this.timestamp = timestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.message = message;
        this.source = source;
    }
}