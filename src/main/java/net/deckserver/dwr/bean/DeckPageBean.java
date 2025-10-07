package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.storage.json.deck.ExtendedDeck;

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
        this.tags = JolAdmin.getAvailableGameFormats(playerName).stream().map(GameFormat::getLabel).toList();
        this.deckFilter = model.getDeckFilter();
    }

}
