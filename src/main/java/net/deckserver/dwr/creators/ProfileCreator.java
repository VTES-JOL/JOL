package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.ProfileBean;
import net.deckserver.dwr.model.PlayerModel;

public class ProfileCreator implements ViewCreator {
    @Override
    public String getFunction() {
        return "callbackProfile";
    }

    @Override
    public Object createData(AdminBean abean, PlayerModel model) {
        return new ProfileBean(abean, model);
    }
}
