/*
 * Deck.java
 *
 * Created on September 25, 2003, 8:45 PM
 */

package deckserver.game.cards;

import net.deckserver.jol.game.cards.CardEntry;

/**
 * @author administrator
 */
public interface Deck {

    CardEntry[] getCards();

    int getQuantity(CardEntry card);

}
