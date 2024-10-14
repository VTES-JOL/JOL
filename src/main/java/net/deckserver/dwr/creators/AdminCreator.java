package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminPageBean;
import net.deckserver.dwr.model.PlayerModel;

public class AdminCreator implements ViewCreator {
    @Override
    public String getFunction() {
        return "callbackAdmin";
    }

    @Override
    public Object createData(PlayerModel model) {
        return new AdminPageBean(model);
    }
}
