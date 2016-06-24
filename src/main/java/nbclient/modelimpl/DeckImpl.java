/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package nbclient.modelimpl;

import cards.model.CardEntry;
import cards.model.Deck;

import java.util.HashMap;
import java.util.Map;

/**
 * @author administrator
 */
public class DeckImpl implements Deck {

    Map<CardEntry, Integer> cards = new HashMap<CardEntry, Integer>();

    public void addCard(CardEntry card) {
        cards.put(card, new Integer(1));
    }

    public CardEntry[] getCards() {
        CardEntry[] ret = new CardEntry[cards.size()];
        cards.keySet().toArray(ret);
        return ret;
    }

    public int getQuantity(CardEntry card) {
        Integer i = cards.get(card);
        return i.intValue();
    }

    public void removeAllCards() {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
    }

    public void removeCard(CardEntry card) {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
    }

    public void setQuantity(CardEntry card, int quantity) {
        cards.put(card, new Integer(quantity));
    }

}
