package net.deckserver.game.storage.cards.importer;

import lombok.Data;

import java.util.Set;

@Data
public class Names {

    private String displayName;
    private String uniqueName;
    private Set<String> names;
}
