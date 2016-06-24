package deckserver.client;

import deckserver.interfaces.Card;
import deckserver.interfaces.CardContainer;
import deckserver.interfaces.SCard;

import java.util.LinkedList;

class CardBox extends Notes implements CardContainer {

    final LinkedList<Card> cards = new LinkedList<Card>();
    private final DsGame game;

    public CardBox(DsGame game) {
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

    public SCard[] getCards() {
        return cards.toArray(new Card[0]);
    }

    public void setCards(Card[] c) {
        cards.clear();
        for (int i = 0; i < c.length; i++) {
            Card n = new DsCard(c[i].getId(), c[i].getCardId());
            addCard(n, false);
            getGame().addCard(n);
        }
    }

}
