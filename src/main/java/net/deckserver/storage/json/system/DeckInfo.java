package net.deckserver.storage.json.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckInfo {
    private String deckId;
    private String deckName;
    private DeckFormat format;
    private Set<String> gameFormats = new HashSet<>();
}
