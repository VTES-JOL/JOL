/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model;

/**
 * @author administrator
 */
public interface Recorder {

    public void addCommand(String text, String[] command);

    public void addMessage(String text);

    public GameAction[] getAllActions();

    public GameAction[] getRecentActions(int count);

    public GameAction getAction(int actionNumber);

    public int getNumActions();

}
