package net.deckserver.game.storage.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class LibraryCard {

    private String id;
    private String key;
    private String jolId;
    private String name;
    private String text;
    private List<String> type;
    private String displayName;
    private Set<String> names;
    private Set<String> partials;

    private boolean unique;

    // Costs
    private String blood;
    private String pool;
    private String conviction;

    // Requirements
    private List<String> disciplines;
    private List<String> clans;
    private List<String> requirements;

    // Other
    private Boolean burnOption;
    private Boolean banned;
}
