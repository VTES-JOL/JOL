/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package nbclient.model.state;

/**
 *
 * @author  administrator
 */
public interface SGame  {
    
    public String getName();
    
    public String[] getPlayers();
    
    public SLocation[] getPlayerLocations(String player);
    
    public SLocation getPlayerLocation(String player, String regionName);
        
    public SLocation getLocation(String regionName);
    
    /**
     * if the location is a player location, get the regionName associated,
     * otherwise return null;
     */
    public String getPlayerRegionName(SLocation location);
    
    public SCard getCard(String id);
    
    public SCardContainer getRegionFromCard(SCard card);
    
}
