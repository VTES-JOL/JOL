package deckserver.dwr.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckBean {

    private List<String> decks = new ArrayList<>();
    private List<DeckSummaryBean> games = new ArrayList<>();

    public DeckBean(List<String> decks, List<DeckSummaryBean> games) {
        this.decks = decks;
        this.games = games;
        Collections.sort(decks);
    }

    public List<String> getDecks() {
        return decks;
    }

    public List<DeckSummaryBean> getGames() {
        return games;
    }
}
