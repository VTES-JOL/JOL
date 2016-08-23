/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.game.state;

/**
 * @author administrator
 */
public interface CardContainer {

    void addCard(Card card, boolean first);

    void removeCard(Card card);

    void setCards(Card[] cards);

    Card[] getCards();
}
