/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package net.deckserver.game.interfaces.state;

/**
 * @author administrator
 */
public interface Card extends NoteTaker, CardContainer {

    String getName();

    CardContainer getParent();

    String getId();

    String getCardId();

    Card[] getCards();

    String getOwner();
}
