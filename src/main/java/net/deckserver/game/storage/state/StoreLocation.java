/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package net.deckserver.game.storage.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;
import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.jaxb.state.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

/**
 * @author administrator
 */
public class StoreLocation implements Location {

    private final static Logger logger = LoggerFactory.getLogger(StoreLocation.class);

    StoreGame game;
    Region region;

    /**
     * Creates a new instance of StoreLocation
     */
    public StoreLocation(StoreGame game, Region region) {
        this.game = game;
        this.region = region;
    }

    public Card getCard(String id) {
        Card card = game.getCard(id);
        if (card.getParent().equals(this)) return card;
        return null;
    }

    public Card getCard(int index) {
        return game.getCard(region.getGameCard().get(index).getId());
    }

    public Card[] getCards() {
        List<GameCard> cards = region.getGameCard();
        StoreCard[] ret = new StoreCard[cards.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = new StoreCard(game, cards.get(i));
        return ret;
    }

    public void setCards(Card[] cards) {
        region.getGameCard().clear();
        for (Card card : cards) {
            region.getGameCard().add(game.mkCard(card));
        }
    }

    public Card getFirstCard() {
        return new StoreCard(game, region.getGameCard().get(0));
    }

    public String getName() {
        return region.getName();
    }

    public List<Notation> getNotes() {
        return region.getNotation();
    }

    public void addCard(Card card, boolean first) {
        StoreCard impl = (StoreCard) game.getCard(card.getId());
        GameCard gc = impl.gamecard;
        if (first) {
            List<GameCard> old = region.getGameCard();
            region.getGameCard().clear();
            region.getGameCard().add(gc);
        } else
            region.getGameCard().add(gc);
    }

    public Notation addNote(String name) {
        Notation note = new Notation();
        note.setName(name);
        region.getNotation().add(note);
        return note;
    }

    public void initCards(List<String> cardIds, String owner) {
        region.getGameCard().clear();
        for (String cardId : cardIds) {
            region.getGameCard().add(game.mkCard(cardId, owner));
        }
    }

    public void removeCard(Card card) {
        StoreCard tmp = (StoreCard) game.getCard(card.getId());
        region.getGameCard().remove(tmp.gamecard);
    }

    // PENDING this should return long - the seed used to generate the shuffle, so
    // the exact shuffle can be re-done.  to do this, the static variable needs to be SecureRandom
    // and the random number generation should be - call generateSeed on SecureRandom to update the seed
    // then get a long value from there, using that to initialize a transient Random object, from
    // which we do the shuffle.  The long value can be returned so the shuffle can be replicated.
    public void shuffle(int num) {
        List<GameCard> cards = region.getGameCard();
        List<GameCard> subList = cards.subList(0, num);
        logger.debug("pre-shuffle: {}", subList);
        for (int x = 0; x < 20; x++) {
            Collections.shuffle(subList, new SecureRandom());
            logger.debug("post-shuffle: {}", subList);
        }
    }

}
