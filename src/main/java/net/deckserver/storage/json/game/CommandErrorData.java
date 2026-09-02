package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A failed command attempt, as served to a judge. Parallel to {@link ChatData}
 * but never part of the chat log itself.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class CommandErrorData {

    private static final DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");

    private String timestamp;
    /**
     * Full-precision ISO-8601 attempt time ({@code game_command_error.occurred_at}),
     * paralleling {@link ChatData#getPostedAt()}. The judges' chat log sorts the
     * merged chat/attempt stream on this so a mistype lands immediately before the
     * corrected retry, not merely somewhere in the same minute.
     */
    private String occurredAt;
    private String player;
    private String command;
    private String error;

    public CommandErrorData() {
    }

    public CommandErrorData(String player, String command, String error) {
        OffsetDateTime now = OffsetDateTime.now();
        this.timestamp = now.format(SIMPLE_FORMAT);
        this.occurredAt = now.toString();
        this.player = player;
        this.command = command;
        this.error = error;
    }
}
