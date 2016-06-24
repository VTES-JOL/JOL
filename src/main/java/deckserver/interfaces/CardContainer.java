/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface CardContainer extends SCardContainer {

    void addCard(Card card, boolean first);

    void removeCard(Card card);

    void setCards(Card[] cards);

}
