package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.AllGamesBean;
import net.deckserver.dwr.model.PlayerModel;

public class AllGamesCreator implements ViewCreator {

    @Override
    public String getFunction() {
        return "callbackAllGames";
    }

    @Override
    public Object createData(AdminBean abean, PlayerModel model) {
        return new AllGamesBean(abean, model);
    }
}
