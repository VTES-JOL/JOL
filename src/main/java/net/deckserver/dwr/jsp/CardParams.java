/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package net.deckserver.dwr.jsp;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;

/**
 * @author Joe User
 */
public class CardParams {

    private Card card;
    private final CardSummary summary;
    private boolean hidden = false;

    public CardParams(Card card) {
        this(card, false);
    }

    CardParams(Card card, boolean hidden) {
        this.card = card;
        this.hidden = hidden;
        this.summary = CardSearch.INSTANCE.get(this.card.getCardId());
    }

    public CardParams(CardSummary summary) {
        this.summary = summary;
    }

    public String getId() {
        if (summary != null) return summary.getId();
        return card.getCardId();
    }

    public Card getCard() {
        return card;
    }

    public CardSummary getSummary() {
        return summary;
    }

    public String getName() {
        return summary.getDisplayName() + (summary.isAdvanced() ? "<i class='adv'/>" : "");
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean doNesting() {
        return card != null;
    }

}    
