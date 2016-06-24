/*
 * GameAction.java
 *
 * Created on December 30, 2003, 5:25 PM
 */

package deckserver.interfaces;

/**
 * @author gfink
 */
public interface GameAction {

    int getSequence();

    boolean isCommand();

    String getText();

    String[] command();

    /* should be same as getText */
    String toString();

}
