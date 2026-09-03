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
    /**
     * Per-card display detail (clan / disciplines / path / cost / …) for every
     * distinct card in {@link #selectedDeck}, keyed by card id as a string.
     * Lets the editor render icons on first paint instead of firing a
     * follow-up {@code /cards/details} request; empty when no deck is loaded.
     */
    private final Map<String, CardDetailBean> details;

    public DeckPageBean(ExtendedDeck selectedDeck, String contents, List<String> tags,
                        String deckId, Map<String, DeckValidity> formatValidity,
                        Map<String, CardDetailBean> details) {
        this.selectedDeck = selectedDeck;
        this.contents = contents;
        this.tags = tags;
        this.deckId = deckId;
        this.formatValidity = formatValidity;
        this.details = details;
    }

}
