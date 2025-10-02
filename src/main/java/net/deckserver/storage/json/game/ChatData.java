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