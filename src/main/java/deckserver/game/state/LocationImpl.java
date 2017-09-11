/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package deckserver.game.state;

import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.Region;

import java.util.Collections;
import java.util.List;

/**
 * @author administrator
 */
public class LocationImpl implements Location {

    GameImpl game;
    Region region;

    /**
     * Creates a new instance of LocationImpl
     */
    public LocationImpl(GameImpl game, Region region) {
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
        CardImpl[] ret = new CardImpl[cards.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = new CardImpl(game, cards.get(i));
        return ret;
    }

    public void setCards(Card[] cards) {
        region.getGameCard().clear();
        for (Card card : cards) {
            region.getGameCard().add(game.mkCard(card));
        }
    }

    public Card getFirstCard() {
        return new CardImpl(game, region.getGameCard().get(0));
    }

    public String getName() {
        return region.getName();
    }

    public Note[] getNotes() {
        return Note.getNotes(region.getNotation());
    }

    public void addCard(Card card, boolean first) {
        CardImpl impl = (CardImpl) game.getCard(card.getId());
        GameCard gc = impl.gamecard;
        if (first) {
            List<GameCard> old = region.getGameCard();
            region.getGameCard().clear();
            region.getGameCard().add(gc);
        } else
            region.getGameCard().add(gc);
    }

    public Note addNote(String name) {
        return Note.mkNote(region, name);
    }

    public void initCards(String[] cardIds) {
        region.getGameCard().clear();
        for (String cardId : cardIds) {
            region.getGameCard().add(game.mkCard(cardId));
        }
    }

    public void removeCard(Card card) {
        CardImpl tmp = (CardImpl) game.getCard(card.getId());
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
        Collections.shuffle(subList);
    }

}
