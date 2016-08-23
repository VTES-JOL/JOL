/*
 * Deck.java
 *
 * Created on September 25, 2003, 8:45 PM
 */

package deckserver.game.cards;

/**
 * @author administrator
 */
public interface Deck {

    CardEntry[] getCards();

    int getQuantity(CardEntry card);

    String getDeckString();

    CardSet findCardName(String text);

    String[] getErrorLines();

    int getCryptSize();

    int getLibSize();

    String getGroups();
}
