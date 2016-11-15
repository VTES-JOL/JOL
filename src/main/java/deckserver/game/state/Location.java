/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.game.state;

/**
 * @author administrator
 */
public interface Location extends NoteTaker, CardContainer {

    void initCards(String[] cardIds);

    void shuffle(int num);

    String getName();

    Card getCard(int index);

    Card getFirstCard();

    Card[] getCards();
}
