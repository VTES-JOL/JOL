/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package net.deckserver.game.interfaces.turn;

import java.util.List;

/**
 * @author administrator
 */
public interface TurnRecorder {

    void addTurn(String meth, String label);

    List<String> getTurns();

    String getMethTurn(String label);

    void addCommand(String turn, String text, String[] command);

    void addMessage(String turn, String text);

    GameAction[] getActions(String turn);

    int getCounter();

}
