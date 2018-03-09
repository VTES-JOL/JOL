/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package net.deckserver.game.interfaces.state;

import java.util.List;

/**
 * @author administrator
 */
public interface Game extends NoteTaker {

    void setName(String name);

    void addPlayer(String player);

    void orderPlayers(List<String> order);

    void addLocation(String regionName);

    void addLocation(String player, String regionName);

    String getName();

    List<String> getPlayers();

    Location[] getPlayerLocations(String player);

    Location getPlayerLocation(String player, String regionName);

    Location getLocation(String regionName);

    String getPlayerRegionName(Location location);

    Card getCard(String id);

    CardContainer getRegionFromCard(Card card);
}
