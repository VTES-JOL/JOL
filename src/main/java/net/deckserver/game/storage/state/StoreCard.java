/*
 * CardImpl.java
 *
 * Created on September 19, 2003, 8:42 PM
 */

package net.deckserver.game.storage.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;
import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;

import java.util.List;

/**
 * @author administrator
 */
public class StoreCard implements Card {

    private StoreGame game;
    GameCard gamecard;

    /**
     * Creates a new instance of StoreCard
     */
    public StoreCard(StoreGame game, GameCard gamecard) {
        this.game = game;
        this.gamecard = gamecard;
    }

    public String getCardId() {
        return gamecard.getCardid();
    }

    public String getId() {
        return gamecard.getId();
    }

    public String getName() {
        CardSummary card = CardSearch.INSTANCE.get(getCardId());
        if (card == null) return getCardId();
        return card.getName();
    }

    public List<Notation> getNotes() {
        return gamecard.getNotation();
    }

    public String getOwner() {
        return gamecard.getOwner();
    }

    public void addCard(Card card, boolean first) {
        CardContainer container = game.getCardContainer(this, true);
        container.addCard(card, first);
    }

    public Notation addNote(String name) {
        Notation note = new Notation();
        note.setName(name);
        gamecard.getNotation().add(note);
        return note;
    }

    public Card[] getCards() {
        CardContainer container = game.getCardContainer(this, false);
        if (container == null) return new Card[0];
        return container.getCards();
    }

    public void setCards(Card[] cards) {
        for (Card card : cards) {
            GameCard c = game.mkCard(card);
            Card cd = game.getCard(c.getId());
            addCard(cd, false);
        }
    }

    public CardContainer getParent() {
        return game.getContainer(this);
    }

    public void removeCard(Card card) {
        CardContainer container = game.getCardContainer(this, false);
        container.removeCard(card);
    }

    public String toString() {
        return getName();
    }

}
