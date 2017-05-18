/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package deckserver.game.state;

import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Region;

import java.util.*;

/**
 * @author administrator
 */
public class GameImpl implements Game {

    public GameState state;
    private Map<String, LocationImpl> regionCache = new HashMap<>();
    private Map<String, CardImpl> cardCache = new HashMap<>();

    /**
     * Creates a new instance of CardImpl
     */
    public GameImpl(GameState state) {
        this.state = state;
        createCache();
    }

    private void createCache() {
        List<Region> regions = state.getRegion();
        for (Region region : regions) {
            regionCache.put(region.getName(), new LocationImpl(this, region));
            List<GameCard> cards = region.getGameCard();
            for (GameCard card : cards) {
                cardCache.put(card.getId(), new CardImpl(this, card));
            }
        }
    }

    public Note[] getNotes() {
        return Note.getNotes(state.getNotation());
    }

    public Location getLocation(String regionName) {
        return getLocation(regionName, false);
    }

    public Location getLocation(final String regionName, boolean add) {
        LocationImpl arr = regionCache.get(regionName);
        if (arr == null && add) arr = addLocationImpl(regionName);
        return arr;
    }

    private LocationImpl[] getLocations(StringFilter filter) {
        List<Region> regions = state.getRegion();
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

    public Location[] getPlayerLocations(final String player) {
        return getLocations(new StringFilter() {
            public boolean accept(String str) {
                return str.startsWith(player + "'s ");
            }
        });
    }

    public String[] getPlayers() {
        return state.getPlayer().toArray(new String[0]);
    }

    public void orderPlayers(String[] order) {
        List<String> playerOrder = state.getPlayer();
        playerOrder.clear();
        playerOrder.addAll(Arrays.asList(order));
    }

    private LocationImpl addLocationImpl(String regionName) {
        Region region = new Region();
        region.setName(regionName);
        state.getRegion().add(region);
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

    public Location getPlayerLocation(String player, String regionName) {
        return getLocation(player + "'s " + regionName);
    }

    public void addPlayer(String player) {
        state.getPlayer().add(player);
    }

    LocationImpl getCardContainer(CardImpl card, boolean create) {
        String name = cardRegionName(card);
        LocationImpl loc = regionCache.get(name);
        if (loc == null && create) loc = addLocationImpl(name);
        return loc;
    }

    CardContainer getContainer(CardImpl card) {
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

    public Card getCard(String id) {
        return cardCache.get(id);
    }

    public Note addNote(String name) {
        return Note.mkNote(state, name);
    }

    public String getPlayerRegionName(Location location) {
        String name = location.getName();
        int index = name.lastIndexOf("'s ");
        if (index > 0) return name.substring(index + 3);
        return null;
    }

    public CardContainer getRegionFromCard(Card card) {
        CardContainer container = getContainer((CardImpl) card);
        if (container instanceof Card) return getRegionFromCard((Card) container);
        return container;
    }

    static abstract class StringFilter {
        abstract public boolean accept(String str);
    }

}
