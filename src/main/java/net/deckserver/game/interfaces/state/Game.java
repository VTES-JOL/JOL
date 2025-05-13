/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package net.deckserver.game.interfaces.state;

import net.deckserver.game.storage.state.RegionType;

import java.util.List;

/**
 * @author administrator
 */
public interface Game extends NoteTaker {

    void setName(String name);

    void addPlayer(String player);

    void orderPlayers(List<String> order);

    void addLocation(String player, RegionType type);

    String getName();

    List<String> getPlayers();

    Location[] getPlayerLocations(String player);

    Location getPlayerLocation(String player, RegionType regionType);

    String getPlayerRegionName(Location location);

    Card getCard(String id);

    CardContainer getRegionFromCard(Card card);
}
