package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.storage.json.deck.ExtendedDeck;

import java.util.List;

@Getter
public class DeckPageBean {

    private final ExtendedDeck selectedDeck;
    private final String contents;
    private final List<String> tags;

    public DeckPageBean(ExtendedDeck selectedDeck, String contents, List<String> tags) {
        this.selectedDeck = selectedDeck;
        this.contents = contents;
        this.tags = tags;
    }

}
