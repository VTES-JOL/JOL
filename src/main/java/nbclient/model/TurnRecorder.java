/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model;

/**
 * @author administrator
 */
public interface TurnRecorder {

    public void addTurn(String meth, String label);

    public String[] getTurns();

    public String getMethTurn(String label);

    public void addCommand(String turn, String text, String[] command);

    public void addMessage(String turn, String text);

    public GameAction[] getActions(String turn);

    public int getCounter();

}
