package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.storage.json.deck.ExtendedDeck;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DeckPageBean {

    private final List<DeckInfoBean> decks;
    private final ExtendedDeck selectedDeck;
    private final String contents;

    public DeckPageBean(PlayerModel model) {
        String playerName = model.getPlayerName();
        this.decks = JolAdmin.INSTANCE.getDeckNames(playerName).stream()
                .map(deckName -> new DeckInfoBean(playerName, deckName))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        this.selectedDeck = model.getDeck();
        this.contents = model.getContents();
    }

}
