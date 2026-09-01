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
    private String player;
    private String command;
    private String error;

    public CommandErrorData() {
    }

    public CommandErrorData(String player, String command, String error) {
        this.timestamp = OffsetDateTime.now().format(SIMPLE_FORMAT);
        this.player = player;
        this.command = command;
        this.error = error;
    }
}
