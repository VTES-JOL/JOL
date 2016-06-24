/*
 * MkHandFrame.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package deckserver.client;

import deckserver.interfaces.GameAction;
import deckserver.JolAdminFactory;
import deckserver.JolGame;

import java.io.PrintWriter;

/**
 * @author Joe User
 */
public class MkMessages {

    JolGame game;
    String cardUrl = null;

    /**
     * Creates a new instance of MkState
     */
    public MkMessages(String name) {
        game = JolAdminFactory.INSTANCE.getGame(name);
    }

    public void writeMessages(PrintWriter out, int num) {
        if (game == null) {
            out.println("No such game");
        } else {
            GameAction[] actions = game.getActions(game.getCurrentTurn());
            out.println("Recent activity:");
            out.println("<ul>");
            for (int i = 0; i < actions.length; i++) {
                out.println("<li>(" + actions[i].getSequence() + ") " + actions[i].getText() + "</li>");
            }
            out.println("</ul>");
        }
    }

}
