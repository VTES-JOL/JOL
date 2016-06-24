package deckserver.cards;

import deckserver.interfaces.CardEntry;
import deckserver.interfaces.CardSearch;
import deckserver.interfaces.CardSet;
import deckserver.interfaces.NormalizeDeck;

import java.util.Collection;
import java.util.Iterator;

public final class NormalizeDeckFactory {

    public final static NormalizeDeck getNormalizer(CardSearch search, String deck) {
        return new NormalizeDeckImpl(search, deck);
    }

    public final static NormalizeDeck getDeckSize(CardSearch search, String deck) {
        try {
            return new NormalizeDeckImpl(search, deck, true);
        } catch (Exception e) {
            // deck shortcut didn't work
            return getNormalizer(search, deck);
        }
    }

    public final static NormalizeDeck constructDeck(CardSearch search, String deck) {
        return new NormalizeDeckImpl(search, deck, false, true);
    }

    public static CardSet findCardName(CardSearch search, String text, Collection<String> errors) {
        CardSet cards = search.getAllCards();
        // check for prefixes
        CardSet set = search.searchByName(cards, text);
        if (set.getCardArray().length > 0) return set;
        // check for abbreviations
        text = text.toLowerCase();
        String id = search.getId(text);
        if (id == null || id.equals("not found"))
            // check for abbreviation prefixes
            for (Iterator<?> i = search.getNames().iterator(); i.hasNext(); ) {
                String abbrev = (String) i.next();
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
        if (id == null || set.getCardArray().length == 0) {
            errors.add(text);
        }
        return set;
    }

}
