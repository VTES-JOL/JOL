package net.deckserver.dwr.bean;

import net.deckserver.DeckParser;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.storage.json.deck.ExtendedDeck;

public class SelectedDeckBean {

    private final String contents;
    private final ExtendedDeck details;

    public SelectedDeckBean(PlayerModel playerModel) {
        contents = playerModel.getContents();
        details = DeckParser.parseDeck(contents);
        String deckName = playerModel.getDeckName();
        details.getDeck().setName(deckName);
    }

    public String getContents() {
        return contents;
    }

    public ExtendedDeck getDetails() {
        return details;
    }

}
