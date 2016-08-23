package deckserver.game.cards;

import java.util.Collection;

public final class DeckFactory {

    public final static Deck getNormalizer(CardSearch search, String deck) {
        return new DeckImpl(search, deck);
    }

    public final static Deck getDeckSize(CardSearch search, String deck) {
        try {
            return new DeckImpl(search, deck, true);
        } catch (Exception e) {
            // deck shortcut didn't work
            return getNormalizer(search, deck);
        }
    }

    public final static Deck constructDeck(CardSearch search, String deck) {
        return new DeckImpl(search, deck, false, true);
    }

    public static CardEntry[] findCardName(CardSearch search, String text, Collection<String> errors) {
        CardEntry[] cards = search.getAllCards();
        // check for prefixes
        CardEntry[] set = search.searchByName(cards, text);
        if (set.length > 0) return set;
        // check for abbreviations
        text = text.toLowerCase();
        String id = search.getId(text);
        if (id == null || id.equals("not found"))
            // check for abbreviation prefixes
            for (String abbrev : search.getNames()) {
                if (abbrev.startsWith(text)) {
                    id = search.getId(abbrev);
                    break;
                }
            }
        if (id.equals("not found")) id = null;
        if (id != null) {
            // now need to convert this to a set to handle advanced vamps
            CardEntry card = search.getCardById(id);

            set = search.searchByName(cards, card.getBaseName());
        }
        if (id == null || set.length == 0) {
            errors.add(text);
        }
        return set;
    }

}
