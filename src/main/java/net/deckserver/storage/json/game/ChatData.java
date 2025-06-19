package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ChatData {

    private String timestamp;
    private String message;
    private String source;
    private String command;

    public ChatData() {
    }

    public ChatData(String timestamp, String message, String source, String command) {
        this.timestamp = timestamp;
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