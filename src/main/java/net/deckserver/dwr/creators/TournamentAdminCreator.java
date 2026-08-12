package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.TournamentAdminBean;
import net.deckserver.dwr.model.PlayerModel;

public class TournamentAdminCreator implements ViewCreator {
    @Override
    public String getFunction() {
        return "callbackTournamentAdmin";
    }

    @Override
    public Object createData(PlayerModel model) {
        return new TournamentAdminBean(model);
    }
}
