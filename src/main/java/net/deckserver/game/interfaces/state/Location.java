/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package net.deckserver.game.interfaces.state;

import java.util.List;

/**
 * @author administrator
 */
public interface Location extends NoteTaker, CardContainer {

    void initCards(List<String> cardIds, String owner);

    void shuffle(int num);

    String getName();

    Card getCard(int index);

    Card getFirstCard();

    Card[] getCards();

    void setOwner(String owner);
    String getOwner();
}
