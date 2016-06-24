/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface Location extends NoteTaker, SLocation, CardContainer {

    void initCards(String[] cardIds);

    void shuffle(int num);

}
