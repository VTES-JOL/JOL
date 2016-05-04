package deckserver.dwr.bean;

import java.util.Collection;

public class DeckBean {
	
	DeckSummaryBean[] decks, games;

	public DeckBean(DeckSummaryBean[] beans, Collection<DeckSummaryBean> games) {
		decks = beans;
		this.games = games.toArray(new DeckSummaryBean[0]);
	}

	public DeckSummaryBean[] getDecks() {
		return decks;
	}

	public DeckSummaryBean[] getGames() {
		return games;
	}
}
