/*
 * GameAction.java
 *
 * Created on December 30, 2003, 5:25 PM
 */

package nbclient.model;

/**
 * @author gfink
 */
public interface GameAction {

    public int getSequence();

    public boolean isCommand();

    public String getText();

    public String[] command();

    /* should be same as getText */
    public String toString();

}
