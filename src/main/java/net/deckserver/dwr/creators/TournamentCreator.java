package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.TournamentBean;
import net.deckserver.dwr.model.PlayerModel;

public class TournamentCreator implements ViewCreator {

    @Override
    public String getFunction() {
        return "callbackTournament";
    }

    @Override
    public Object createData(PlayerModel model) {
        return new TournamentBean(model);
    }
}
