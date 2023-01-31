package net.deckserver.storage.json.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RegistrationStatus {
    private String deckId;
    private String deckName;
    private boolean valid;
    private String summary;

    public RegistrationStatus(String deckId) {
        this.deckId = deckId;
    }
}
