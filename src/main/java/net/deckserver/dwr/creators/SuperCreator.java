package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.SuperAdminBean;
import net.deckserver.dwr.model.PlayerModel;

public class SuperCreator implements ViewCreator {
    @Override
    public String getFunction() {
        return "callbackSuper";
    }

    @Override
    public Object createData(PlayerModel model) {
        return new SuperAdminBean(model);
    }
}
