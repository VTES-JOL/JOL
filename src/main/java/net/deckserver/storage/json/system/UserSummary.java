package net.deckserver.storage.json.system;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserSummary {
    private String name;
    private OffsetDateTime lastOnline;
    private List<String> roles = new ArrayList<>();
    private String country;

    public String getLastOnline() {
        return lastOnline.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
