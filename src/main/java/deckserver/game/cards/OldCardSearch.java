/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package deckserver.game.cards;

import net.deckserver.jol.game.cards.CardEntry;

import java.util.Set;

/**
 * @author administrator
 */
public interface OldCardSearch {

    String getId(String nm);

    Set<String> getNames();

    CardSet getAllCards();

    CardEntry getCardById(String id);

    CardSet searchByType(CardSet set, String type);

    CardSet searchByText(CardSet set, String text);

    CardSet searchByName(CardSet set, String name);

}
