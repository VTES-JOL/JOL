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