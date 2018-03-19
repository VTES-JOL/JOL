package net.deckserver.dwr.bean;

import net.deckserver.game.storage.cards.CardType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class DeckBean {

    private List<String> decks;
    private List<DeckSummaryBean> games;
    private List<String> types;

    public DeckBean(List<String> decks, List<DeckSummaryBean> games) {
        this.decks = decks;
        this.games = games;
        this.types = EnumSet.allOf(CardType.class).stream().map(CardType::getLabel).collect(Collectors.toList());
        Collections.sort(decks);
    }

    public List<String> getDecks() {
        return decks;
    }

    public List<DeckSummaryBean> getGames() {
        return games;
    }

    public List<String> getTypes() {
        return types;
    }
}
