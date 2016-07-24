/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package deckserver.game.state;

import deckserver.game.state.model.GameCard;
import deckserver.game.state.model.GameState;
import deckserver.game.state.model.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author administrator
 */
public class GameImpl implements Game {

    public GameState state;
    Map<String, LocationImpl> regionCache = new HashMap<>();
    Map<String, CardImpl> cardCache = new HashMap<>();

    /**
     * Creates a new instance of CardImpl
     */
    public GameImpl(GameState state) {
        this.state = state;
        createCache();
    }

    private void createCache() {
        Region[] regions = state.getRegion();
        for (Region region : regions) {
            regionCache.put(region.getName(), new LocationImpl(this, region));
            GameCard[] cards = region.getGameCard();
            for (GameCard card : cards) {
                cardCache.put(card.getId(), new CardImpl(this, card));
            }
        }
    }

    public Note[] getNotes() {
        return NoteImpl.getNotes(state.getNotation());
    }

    public SLocation getLocation(String regionName) {
        return getLocation(regionName, false);
    }

    public Location getLocation(final String regionName, boolean add) {
        LocationImpl arr = regionCache.get(regionName);
        if (arr == null && add) arr = addLocationImpl(regionName);
        return arr;
    }

    private LocationImpl[] getLocations(StringFilter filter) {
        Region[] regions = state.getRegion();
        ArrayList<LocationImpl> list = new ArrayList<>();
        for (Region region : regions)
            if (filter.accept(region.getName())) {
                LocationImpl location = regionCache.get(region.getName());
                list.add(location);
            }
        LocationImpl[] ret = new LocationImpl[list.size()];
        return list.toArray(ret);
    }

    public String getName() {
        return state.getName();
    }

    public void setName(String name) {
        state.setName(name);
    }

    public SLocation[] getPlayerLocations(final String player) {
        return getLocations(new StringFilter() {
            public boolean accept(String str) {
                return str.startsWith(player + "'s ");
            }
        });
    }

    public String[] getPlayers() {
        return state.getPlayer();
    }

    public void orderPlayers(String[] order) {
        state.setPlayer(null);
        state.setPlayer(order);
    }

    private LocationImpl addLocationImpl(String regionName) {
        Region region = new Region();
        region.setName(regionName);
        state.addRegion(region);
        LocationImpl loc = new LocationImpl(this, region);
        regionCache.put(regionName, loc);
        return loc;
    }

    public void addLocation(String regionName) {
        addLocationImpl(regionName);
    }

    public void addLocation(String player, String regionName) {
        addLocation(player + "'s " + regionName);
    }

    public SLocation getPlayerLocation(String player, String regionName) {
        return getLocation(player + "'s " + regionName);
    }

    public void addPlayer(String player) {
        state.addPlayer(player);
    }

    LocationImpl getCardContainer(CardImpl card, boolean create) {
        String name = cardRegionName(card);
        LocationImpl loc = regionCache.get(name);
        if (loc == null && create) loc = addLocationImpl(name);
        return loc;
    }

    CardContainer getContainer(CardImpl card) {
        Region region = (Region) card.gamecard.parent();
        String name = region.getName();
        if (name.startsWith("ZZZ"))
            return (CardContainer) getCard(getCardIdFromRegionName(name));
        else return regionCache.get(name);
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

    GameCard mkCard(String cardid) {
        String id = getCount() + "";
        return mkCard(cardid, id);
    }

    GameCard mkCard(String cardid, String id) {
        GameCard card = new GameCard();
        card.setCardid(cardid);
        card.setId(id);
        cardCache.put(id, new CardImpl(this, card));
        return card;
    }

    GameCard mkCard(Card card) {
        return mkCard(card.getCardId(), card.getId());
    }

    public SCard getCard(String id) {
        return cardCache.get(id);
    }

    public Note addNote(String name) {
        return NoteImpl.mkNote(state, name);
    }

    public String getPlayerRegionName(SLocation location) {
        String name = location.getName();
        int index = name.lastIndexOf("'s ");
        if (index > 0) return name.substring(index + 3);
        return null;
    }

    public SCardContainer getRegionFromCard(SCard card) {
        CardContainer container = getContainer((CardImpl) card);
        if (container instanceof Card) return getRegionFromCard((Card) container);
        return container;
    }

    static abstract class StringFilter {
        abstract public boolean accept(String str);
    }

}
