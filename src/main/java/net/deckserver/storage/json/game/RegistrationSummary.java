package net.deckserver.storage.json.game;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Data
public class RegistrationSummary {
    private String name;
    private OffsetDateTime timestamp;
    // Map of players -> registration summary
    private Map<String, String> players = new HashMap<>();

    public String getTimestamp() {
        return timestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
