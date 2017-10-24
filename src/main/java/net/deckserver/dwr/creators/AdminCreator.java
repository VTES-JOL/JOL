package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.AdminPageBean;
import net.deckserver.dwr.model.PlayerModel;

class AdminCreator implements ViewCreator {

    public String getFunction() {
        return "callbackAdmin";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        return new AdminPageBean(abean, model.getPlayer());
    }

}
