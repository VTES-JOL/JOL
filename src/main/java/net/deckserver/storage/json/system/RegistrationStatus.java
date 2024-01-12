package net.deckserver.storage.json.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RegistrationStatus {
    private String deckId;
    private String deckName;
    private boolean valid;
    private String summary;
    private OffsetDateTime timestamp;

    public RegistrationStatus(String deckId) {
        this.deckId = deckId;
        this.timestamp = OffsetDateTime.now();
    }

    public RegistrationStatus(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
