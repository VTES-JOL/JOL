package net.deckserver.dwr.bean;

import net.deckserver.game.storage.cards.CardType;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class DeckBean {

    private List<DeckInfoBean> decks;
    private List<String> types;

    public DeckBean(List<DeckInfoBean> decks) {
        this.decks = decks;
        this.types = EnumSet.allOf(CardType.class).stream().map(CardType::getLabel).collect(Collectors.toList());
    }

    public List<DeckInfoBean> getDecks() {
        return decks;
    }


    public List<String> getTypes() {
        return types;
    }
}
