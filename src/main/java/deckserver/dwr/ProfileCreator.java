package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.ProfileBean;

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
