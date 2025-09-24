package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.GameFormat;

import java.util.List;

@Getter
public class DeckPageBean {

    private final ExtendedDeck selectedDeck;
    private final String contents;
    private final List<String> tags;
    private final String deckFilter;

    public DeckPageBean(PlayerModel model) {
        String playerName = model.getPlayerName();
        this.selectedDeck = model.getDeck();
        this.contents = model.getContents();
        this.tags = JolAdmin.INSTANCE.getAvailableGameFormats(playerName).stream().map(GameFormat::getLabel).toList();
        this.deckFilter = model.getDeckFilter();
    }

}
