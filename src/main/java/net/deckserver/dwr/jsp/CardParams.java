/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package net.deckserver.dwr.jsp;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;

/**
 * @author Joe User
 */
public class CardParams {

    private Card card;
    private CardEntry entry;
    private boolean hidden = false;

    public CardParams(Card card) {
        this(card, false);
    }

    CardParams(Card card, boolean hidden) {
        this.card = card;
        this.hidden = hidden;
        this.entry = CardSearch.INSTANCE.getCardById(this.card.getCardId());
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
