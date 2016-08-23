package deckserver.game.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DsGame extends Notes implements Game {

    private final LinkedList<Player> players = new LinkedList<>();
    private final LinkedList<LocBox> regions = new LinkedList<>();
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

    public void orderPlayers(String[] order) {
        Collection<Player> newP = new LinkedList<>();
        for (String anOrder : order) {
            newP.add(getPlayer(anOrder));
        }
        players.clear();
        players.addAll(newP);
    }

    public void addLocation(String regionName) {
        LocBox l = new LocBox(regionName, this);
        regions.add(l);
    }

    public void addLocation(String player, String regionName) {
        LocBox l = new LocBox(regionName, this);
        Player p = getPlayer(player);
        p.locs.add(l);
    }

    public String getName() {
        return gname;
    }

    public void setName(String name) {
        gname = name;
    }

    public String[] getPlayers() {
        String[] ret = new String[players.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = players.get(i).name;
        return ret;
    }

    public Location[] getPlayerLocations(String player) {
        Player p = getPlayer(player);
        return p.locs.toArray(new Location[0]);
    }

    public Location getPlayerLocation(String player, String regionName) {
        Player p = getPlayer(player);
        for (LocBox l : p.locs) {
            if (l.getName().equals(regionName)) return l;
        }
        return null;
    }

    public Location getLocation(String regionName) {
        for (LocBox l : regions) {
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

    static class Player {

        final String name;
        final Collection<LocBox> locs = new LinkedList<>();

        Player(String name) {
            this.name = name;
        }
    }
}
