package net.deckserver.dwr.bean;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ChatEntryBean {

    private String timestamp;
    private String player;
    private String message;

    public ChatEntryBean(String player, String message) {
        this.timestamp = OffsetDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.player = player;
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }
}
