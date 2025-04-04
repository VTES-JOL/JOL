/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package net.deckserver.game.storage.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;
import net.deckserver.game.interfaces.state.Game;
import net.deckserver.game.interfaces.state.Location;
import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.jaxb.state.Region;

import java.util.*;

/**
 * @author administrator
 */
public class StoreGame implements Game {

    public GameState state;
    private Map<String, StoreLocation> regionCache = new HashMap<>();
    private Map<String, StoreCard> cardCache = new HashMap<>();

    /**
     * Creates a new instance of StoreGame
     */
    public StoreGame(GameState state) {
        this.state = state;
        List<Region> regions = state.getRegion();
        for (Region region : regions) {
            regionCache.put(region.getName(), new StoreLocation(this, region));
            List<GameCard> cards = region.getGameCard();
            for (GameCard card : cards) {
                cardCache.put(card.getId(), new StoreCard(this, card));
            }
        }
    }

    public List<Notation> getNotes() {
        return state.getNotation();
    }

    public Location getLocation(String regionName) {
        return getLocation(regionName, false);
    }

    public Location getLocation(final String regionName, boolean add) {
        StoreLocation arr = regionCache.get(regionName);
        if (arr == null && add) arr = addLocationImpl(regionName);
        return arr;
    }

    private StoreLocation[] getLocations(StringFilter filter) {
        List<Region> regions = state.getRegion();
        ArrayList<StoreLocation> list = new ArrayList<>();
        for (Region region : regions)
            if (filter.accept(region.getName())) {
                StoreLocation location = regionCache.get(region.getName());
                list.add(location);
            }
        StoreLocation[] ret = new StoreLocation[list.size()];
        return list.toArray(ret);
    }

    public String getName() {
        return state.getName();
    }

    public void setName(String name) {
        state.setName(name);
    }

    public Location[] getPlayerLocations(final String player) {
        return getLocations(new StringFilter() {
            public boolean accept(String str) {
                return str.startsWith(player + "'s ");
            }
        });
    }

    public List<String> getPlayers() {
        return state.getPlayer();
    }

    public void orderPlayers(List<String> order) {
        List<String> playerOrder = state.getPlayer();
        playerOrder.clear();
        playerOrder.addAll(order);
    }

    private StoreLocation addLocationImpl(String regionName) {
        Region region = new Region();
        region.setName(regionName);
        state.getRegion().add(region);
        StoreLocation loc = new StoreLocation(this, region);
        regionCache.put(regionName, loc);
        return loc;
    }

    public void addLocation(String player, String regionName) {
        StoreLocation location = addLocationImpl(player + "'s " + regionName);
        location.setOwner(player);
    }

    public Location getPlayerLocation(String player, String regionName) {
        return getLocation(player + "'s " + regionName);
    }

    public void addPlayer(String player) {
        state.getPlayer().add(player);
    }

    StoreLocation getCardContainer(StoreCard card, boolean create) {
        String name = cardRegionName(card);
        StoreLocation loc = regionCache.get(name);
        if (loc == null && create) loc = addLocationImpl(name);
        return loc;
    }

    CardContainer getContainer(StoreCard card) {
        Optional<String> parentRegion = state.getRegion().stream()
                .filter(region -> region.getGameCard().contains(card.gamecard))
                .findFirst()
                .map(Region::getName);
        return parentRegion.map(regionName -> {
            if (regionName.startsWith("ZZZ")) {
                return getCard(getCardIdFromRegionName(regionName));
            } else return regionCache.get(regionName);
        }).orElseThrow(() -> new IllegalArgumentException("Card doesn't have a parent region"));
    }

    private String getCardIdFromRegionName(String name) {
        int index = name.indexOf(" ");
        return name.substring(3, index);
    }

    private String cardRegionName(Card card) {
        return "ZZZ" + card.getId() + " container";
    }

    private int getCount() {
        String count = state.getCounter();
        int num = (count == null) ? 1 : Integer.parseInt(count) + 1;
        state.setCounter(num + "");
        return num;
    }

    GameCard mkCard(String cardid, String owner) {
        String id = getCount() + "";
        return mkCard(cardid, id, owner);
    }

    GameCard mkCard(String cardid, String id, String owner) {
        GameCard card = new GameCard();
        card.setCardid(cardid);
        card.setId(id);
        card.setOwner(owner);
        cardCache.put(id, new StoreCard(this, card));
        return card;
    }

    GameCard mkCard(Card card) {
        return mkCard(card.getCardId(), card.getId(), card.getOwner());
    }

    public Card getCard(String id) {
        return cardCache.get(id);
    }

    public Notation addNote(String name) {
        Notation note = new Notation();
        note.setName(name);
        state.getNotation().add(note);
        return note;
    }

    public String getPlayerRegionName(Location location) {
        String name = location.getName();
        int index = name.lastIndexOf("'s ");
        if (index > 0) return name.substring(index + 3);
        return null;
    }

    public CardContainer getRegionFromCard(Card card) {
        CardContainer container = getContainer((StoreCard) card);
        if (container instanceof Card) return getRegionFromCard((Card) container);
        return container;
    }

    static abstract class StringFilter {
        abstract public boolean accept(String str);
    }

}
