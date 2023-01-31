package net.deckserver.storage.json.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckInfo {
    private String deckId;
    private String deckName;
    private DeckFormat format;
}
