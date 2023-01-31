package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DeckPageBean {

    private final List<DeckInfoBean> decks;
    private SelectedDeckBean selectedDeck;

    public DeckPageBean(PlayerModel model) {
        String playerName = model.getPlayerName();
        this.decks = JolAdmin.getInstance().getDeckNames(playerName).stream()
                .map(deckName -> new DeckInfoBean(playerName, deckName))
                .sorted(Comparator.comparing(DeckInfoBean::getName))
                .collect(Collectors.toList());
        if (model.getContents() != null) {
            selectedDeck = new SelectedDeckBean(model);
        }
    }

    public List<DeckInfoBean> getDecks() {
        return decks;
    }

    public SelectedDeckBean getSelectedDeck() {
        return selectedDeck;
    }
}
