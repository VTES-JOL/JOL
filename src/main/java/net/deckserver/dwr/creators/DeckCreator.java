package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.DeckBean;
import net.deckserver.dwr.bean.DeckSummaryBean;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class DeckCreator implements ViewCreator {

    public String getFunction() {
        return "callbackShowDecks";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        String player = model.getPlayer();
        List<String> decks = model.getDecks();
        List<GameModel> actives = abean.getActiveGames();
        JolAdmin admin = JolAdmin.getInstance();
        List<DeckSummaryBean> games = actives.stream()
                .filter(game -> admin.isOpen(game.getName()))
                .filter(game -> (admin.isInvited(game.getName(), player) || admin.getOwner(game.getName()).equals(player) || admin.getGameDeck(game.getName(), player) != null))
                .map(game -> new DeckSummaryBean(game, model))
                .collect(Collectors.toCollection(ArrayList::new));
        return new DeckBean(decks, games);
    }

}
