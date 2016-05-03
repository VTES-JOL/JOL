package dsclient.modelimpl;

import java.util.Collections;

import util.Shuffle;
import nbclient.model.*;
import nbclient.model.state.SCard;

class LocBox extends CardBox implements Location {
	
	private final String lname;
	
	LocBox(String name, DsGame game) {
		super(game);
		this.lname = name;
	}

	public void initCards(String[] cardIds) {
		cards.clear();
		for(int i = 0; i < cardIds.length; i++) {
			Card c = new DsCard(getGame().getNewId()+"",cardIds[i]);
			addCard(c,false);
			getGame().addCard(c);
		}
	}

	public void shuffle(int num) {
		SCard[] pre = getCards();
		SCard[] post = (SCard[]) Shuffle.shuffle(pre,num);
		cards.clear();
		Collections.addAll(cards,(Card[]) post);
	}

	public String getName() {
		return lname;
	}

	public SCard getCard(int index) {
		return (SCard) cards.get(index);
	}

	public SCard getLastCard() {
		return (SCard) cards.getLast();
	}

	public SCard getFirstCard() {
		return (SCard) cards.getFirst();
	}

}
