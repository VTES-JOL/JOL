package deckserver.dwr;

import deckserver.dwr.bean.DeckBean;
import deckserver.dwr.bean.DeckSummaryBean;
import deckserver.rich.AdminBean;
import deckserver.rich.GameModel;
import deckserver.rich.PlayerModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DeckCreator implements ViewCreator {

    public String getFunction() {
        return "showDecks";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        String player = model.getPlayer();
        DeckSummaryBean[] beans = model.getDecks();
        List<GameModel> actives = abean.getActiveGames();
        Collection<DeckSummaryBean> games = new ArrayList<DeckSummaryBean>();
        for (Iterator i = actives.iterator(); i.hasNext(); ) {
            GameModel game = (GameModel) i.next();
            if (abean.getAdmin().isOpen(game.getName()) &&
                    (abean.getAdmin().isInvited(game.getName(), player) || abean.getAdmin().getOwner(game.getName()).equals(player) || abean.getAdmin().getGameDeck(game.getName(), player) != null)) {
                games.add(new DeckSummaryBean(game, model));
            }
        }
        return new DeckBean(beans, games);
    }

}
