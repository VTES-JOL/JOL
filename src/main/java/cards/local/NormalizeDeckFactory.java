package cards.local;

import cards.model.CardEntry;
import cards.model.CardSearch;
import cards.model.CardSet;

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
        //        System.out.println("Finding card " + text);
        // check for exact matches first
        //  CardEntry card = search.getCardByName(text);
        //    if(card != null) {
        //       System.out.println("Searching prefix");
        CardSet cards = search.getAllCards();
        // check for prefixes
        CardSet set = search.searchByName(cards, text);
        //    System.out.println("Initial size " + set.getCardArray().length);
        if (set.getCardArray().length > 0) return set;
        // check for abbreviations
        text = text.toLowerCase();
        //         System.out.println("Searching abbreviations");
        String id = search.getId(text);
        //    System.out.println("id is " + id);
        if (id == null || id.equals("not found"))
            // check for abbreviation prefixes
            for (Iterator<?> i = search.getNames().iterator(); i.hasNext(); ) {
                String abbrev = (String) i.next();
                //     System.out.println("Checking " + abbrev);
                if (abbrev.startsWith(text)) {
                    //         System.out.println("Found abbrev " + abbrev);
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
            //   }
            // couldn't find any match
            //          System.out.println("No match");
            errors.add(text);
        }
        //  System.out.println("Returning " + set.getCardArray().length);
        return set;
    }

}
