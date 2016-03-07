package dsclient.modelimpl;

import java.util.*;

import nbclient.model.*;

class CardBox extends Notes implements CardContainer {
	
	final LinkedList<Card> cards = new LinkedList<Card>();
	private final DsGame game;

	public CardBox(DsGame game) {
		this.game = game;
	}
	
	DsGame getGame() {
		return game;
	}

	public void setCards(Card[] c) {
		cards.clear();
		for(int i = 0; i < c.length; i++) {
			Card n = new DsCard(c[i].getId(),c[i].getCardId());
			addCard(n,false);
			getGame().addCard(n);
		}
	}
	
	public void addCard(nbclient.model.Card card, boolean first) {
        if(first) cards.addFirst(card);
        else cards.addLast(card);
        ((DsCard) card).setParent(this);
	}

	public void removeCard(nbclient.model.Card card) {
		cards.remove(card);
	}

	public nbclient.model.state.SCard[] getCards() {
		return (Card[]) cards.toArray(new Card[0]);
	}

}
