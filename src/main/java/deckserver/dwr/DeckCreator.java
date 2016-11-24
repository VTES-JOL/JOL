package deckserver.dwr;

import deckserver.client.JolAdmin;
import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.DeckBean;
import deckserver.dwr.bean.DeckSummaryBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class DeckCreator implements ViewCreator {

    public String getFunction() {
        return "callbackShowDecks";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        String player = model.getPlayer();
        DeckSummaryBean[] beans = model.getDecks();
        List<GameModel> actives = abean.getActiveGames();
        JolAdmin admin = JolAdmin.getInstance();
        Collection<DeckSummaryBean> games = actives.stream().filter(game -> admin.isOpen(game.getName()) &&
                (admin.isInvited(game.getName(), player) || admin.getOwner(game.getName()).equals(player) || admin.getGameDeck(game.getName(), player) != null)).map(game -> new DeckSummaryBean(game, model)).collect(Collectors.toCollection(ArrayList::new));
        return new DeckBean(beans, games);
    }

}
