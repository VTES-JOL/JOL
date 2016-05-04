/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model;

/**
 *
 * @author  administrator
 */
public interface Game extends NoteTaker, nbclient.model.state.SGame {
    
    public void setName(String name);
    
    public void addPlayer(String player);
    
    public void orderPlayers(String[] order);
    
    public void addLocation(String regionName);
    
    public void addLocation(String player, String regionName);
    
}
