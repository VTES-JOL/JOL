/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.game.state;

/**
 * @author administrator
 */
public interface SGame {

    String getName();

    String[] getPlayers();

    SLocation[] getPlayerLocations(String player);

    SLocation getPlayerLocation(String player, String regionName);

    SLocation getLocation(String regionName);

    /**
     * if the location is a player location, get the regionName associated,
     * otherwise return null;
     */
    String getPlayerRegionName(SLocation location);

    SCard getCard(String id);

    SCardContainer getRegionFromCard(SCard card);

}
