package net.deckserver.game.ui.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;
import net.deckserver.game.interfaces.state.Game;
import net.deckserver.game.interfaces.state.Location;

import java.util.*;
import java.util.stream.Collectors;

public class DsGame extends DsNoteTaker implements Game {

    private final LinkedList<Player> players = new LinkedList<>();
    private final LinkedList<DsLocation> regions = new LinkedList<>();
    private final Map<String, Card> cards = new HashMap<>(500);
    private String gname;
    private int index = 1;

    public void addPlayer(String player) {
        players.add(new Player(player));
    }

    Player getPlayer(String player) {
        for (Player p : players) {
            if (p.name.equals(player)) return p;
        }
        return null;
    }

    public void orderPlayers(List<String> order) {
        List<Player> newPlayers = new ArrayList<>();
        order.stream().map(this::getPlayer).forEach(newPlayers::add);
        players.clear();
        players.addAll(newPlayers);
    }

    public void addLocation(String regionName) {
        DsLocation l = new DsLocation(regionName, this);
        regions.add(l);
    }

    public void addLocation(String player, String regionName) {
        DsLocation l = new DsLocation(regionName, this);
        Player p = getPlayer(player);
        p.locs.add(l);
    }

    public String getName() {
        return gname;
    }

    public void setName(String name) {
        gname = name;
    }

    public List<String> getPlayers() {
        return players.stream().map(Player::getName).collect(Collectors.toList());
    }

    public Location[] getPlayerLocations(String player) {
        Player p = getPlayer(player);
        return p.locs.toArray(new Location[0]);
    }

    public Location getPlayerLocation(String player, String regionName) {
        Player p = getPlayer(player);
        for (DsLocation l : p.locs) {
            if (l.getName().equals(regionName)) return l;
        }
        return null;
    }

    public Location getLocation(String regionName) {
        for (DsLocation l : regions) {
            if (l.getName().equals(regionName)) return l;
        }
        return null;
    }

    public String getPlayerRegionName(Location location) {
        if (regions.contains(location)) return null;
        return location.getName();
    }

    void addCard(Card card) {
        cards.put(card.getId(), card);
    }

    int getNewId() {
        return index++;
    }

    public Card getCard(String id) {
        return cards.get(id);
    }

    public CardContainer getRegionFromCard(Card card) {
        DsCard p = (DsCard) card;
        while (p.getParent() instanceof DsCard) {
            p = (DsCard) p.getParent();
        }
        return p.getParent();
    }

    public void replacePlayer(String oldPlayer, String newPlayer) {
        getPlayer(oldPlayer).name = newPlayer;
        for (DsLocation region : regions) {
            String regionName = region.getName();
            regionName.replaceFirst(oldPlayer, newPlayer);
        }
        cards.values().stream()
                .filter(c -> c.getOwner().equals(oldPlayer))
                .forEach(c -> c.setOwner(newPlayer));
    }

    static class Player {

        String name;
        final Collection<DsLocation> locs = new LinkedList<>();

        Player(String name) {
            this.name = name;
        }

        private void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
