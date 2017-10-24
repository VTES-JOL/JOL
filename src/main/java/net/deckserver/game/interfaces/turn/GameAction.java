/*
 * GameAction.java
 *
 * Created on December 30, 2003, 5:25 PM
 */

package net.deckserver.game.interfaces.turn;

/**
 * @author gfink
 */
public interface GameAction {

    boolean isCommand();

    String getText();

    String[] command();

}
