package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.storage.json.deck.DeckValidity;
import net.deckserver.storage.json.deck.ExtendedDeck;

import java.util.List;
import java.util.Map;

@Getter
public class DeckPageBean {

    private final ExtendedDeck selectedDeck;
    private final String contents;
    private final List<String> tags;
    /** The selected deck's stable id, or null when nothing is loaded. */
    private final String deckId;
    /** Per-format validation outcome (STANDARD/DUEL/V5), keyed by format name; empty when no deck is loaded. */
    private final Map<String, DeckValidity> formatValidity;

    public DeckPageBean(ExtendedDeck selectedDeck, String contents, List<String> tags,
                        String deckId, Map<String, DeckValidity> formatValidity) {
        this.selectedDeck = selectedDeck;
        this.contents = contents;
        this.tags = tags;
        this.deckId = deckId;
        this.formatValidity = formatValidity;
    }

}
