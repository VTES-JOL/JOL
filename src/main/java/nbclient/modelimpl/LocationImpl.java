/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package nbclient.modelimpl;

import javaclient.gen.GameCard;
import javaclient.gen.Region;
import nbclient.model.Card;
import nbclient.model.Location;
import nbclient.model.Note;
import util.Shuffle;

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
        Card card = (Card) game.getCard(id);
        if (card.getParent().equals(this)) return card;
        return null;
    }

    public nbclient.model.state.SCard getCard(int index) {
        return game.getCard(region.getGameCard(index).getId());
    }

    public nbclient.model.state.SCard[] getCards() {
        GameCard[] cards = region.getGameCard();
        CardImpl[] ret = new CardImpl[cards.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = new CardImpl(game, cards[i]);
        return ret;
    }

    public void setCards(Card[] cards) {
        region.setGameCard(new GameCard[0]);
        for (int i = 0; i < cards.length; i++) {
            region.addGameCard(game.mkCard(cards[i]));
        }
    }

    public nbclient.model.state.SCard getFirstCard() {
        return new CardImpl(game, region.getGameCard(0));
    }

    public nbclient.model.state.SCard getLastCard() {
        GameCard[] cards = region.getGameCard();
        return new CardImpl(game, cards[cards.length - 1]);
    }

    public String getName() {
        return region.getName();
    }

    public Note[] getNotes() {
        return NoteImpl.getNotes(region.getNotation());
    }

    public void addCard(Card card, boolean first) {
        CardImpl impl = (CardImpl) game.getCard(card.getId());
        GameCard gc = impl.gamecard;
        if (first) {
            GameCard[] old = region.getGameCard();
            region.setGameCard(new GameCard[0]);
            region.addGameCard(gc);
            for (int i = 0; i < old.length; i++)
                region.addGameCard(gc);
        } else
            region.addGameCard(gc);
    }

    public Note addNote(String name) {
        return NoteImpl.mkNote(region, name);
    }

    public void initCards(String[] cardIds) {
        region.setGameCard(new GameCard[0]);
        for (int i = 0; i < cardIds.length; i++) {
            region.addGameCard(game.mkCard(cardIds[i]));
        }
    }

    public void removeCard(Card card) {
        CardImpl tmp = (CardImpl) game.getCard(card.getId());
        region.removeGameCard(tmp.gamecard);
    }

    public void removeNote(Note note) {
        NoteImpl impl = (NoteImpl) note;
        region.removeNotation(impl.note);
    }

    // PENDING this should return long - the seed used to generate the shuffle, so
    // the exact shuffle can be re-done.  to do this, the static variable needs to be SecureRandom
    // and the random number generation should be - call generateSeed on SecureRandom to update the seed
    // then get a long value from there, using that to initialize a transient Random object, from
    // which we do the shuffle.  The long value can be returned so the shuffle can be replicated.
    public void shuffle(int num) {
        GameCard[] cards = region.getGameCard();
        //debug_print();
        cards = (GameCard[]) Shuffle.shuffle(cards, num);
        region.setGameCard(null);
        region.setGameCard(cards);
        //debug_print();
    }

}
