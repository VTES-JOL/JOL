package net.deckserver.game.ui.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;

import java.util.LinkedList;

public class DsCardContainer extends DsNoteTaker implements CardContainer {

    final LinkedList<Card> cards = new LinkedList<>();
    private final DsGame game;

    public DsCardContainer(DsGame game) {
        this.game = game;
    }

    DsGame getGame() {
        return game;
    }

    public void addCard(Card card, boolean first) {
        if (first) cards.addFirst(card);
        else cards.addLast(card);
        ((DsCard) card).setParent(this);
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }

    public Card[] getCards() {
        return cards.toArray(new Card[0]);
    }

    public void setCards(Card[] c) {
        cards.clear();
        for (Card aC : c) {
            Card n = new DsCard(aC.getId(), aC.getCardId(), aC.getOwner());
            addCard(n, false);
            getGame().addCard(n);
        }
    }

}
