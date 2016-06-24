/*
 * Deck.java
 *
 * Created on September 25, 2003, 8:45 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface Deck {

    CardEntry[] getCards();

    void addCard(CardEntry card);

    int getQuantity(CardEntry card);

    void setQuantity(CardEntry card, int quantity);

    void removeCard(CardEntry card);

    void removeAllCards();
}
