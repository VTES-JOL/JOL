/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import deckserver.game.state.Card;
import net.deckserver.jol.game.cards.CardEntry;

/**
 * @author Joe User
 */
public class CardParams {

    Card card;
    CardEntry entry;
    boolean hidden = false;

    public CardParams(Card card) {
        this(card, false);
    }

    public CardParams(Card card, boolean hidden) {
        this.card = card;
        this.hidden = hidden;
    }

    public CardParams(CardEntry entry) {
        this.entry = entry;
    }

    public String getId() {
        if (entry != null) return entry.getCardId();
        return card.getCardId();
    }

    public Card getCard() {
        return card;
    }

    public CardEntry getEntry() {
        return entry;
    }

    public String getName() {
        if (entry != null) return entry.getName();
        return card.getName();
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean doNesting() {
        return card != null;
    }

}    
