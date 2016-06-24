/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface Game extends NoteTaker, SGame {

    void setName(String name);

    void addPlayer(String player);

    void orderPlayers(String[] order);

    void addLocation(String regionName);

    void addLocation(String player, String regionName);

}
