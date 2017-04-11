package net.deckserver.game;

import java.util.List;

public interface Game extends Notation {

    String getName();

    // Add Player to this game
    void addPlayer(Player player);

    // Get all players
    List<Player> getPlayers();

    // Change the order of players
    void orderPlayers(List<Player> playerList);

    // Add a region to game
    void addRegion(Region region);

    // Add a region to a player
    void addRegion(Player player, Region region);

    // Get all regions for a player
    List<Region> getRegions(Player player);

    // Get a region by player and name
    Region getRegion(Player player, String name);

    // Get a region by name
    Region getRegion(String name);

    // Get a region by card
    Region getRegion(Card card);

    // Get a card by instance id
    Card getCard(String instanceId);

}
