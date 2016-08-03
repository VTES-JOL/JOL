package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.AdminPageBean;

class AdminCreator implements ViewCreator {

    public String getFunction() {
        return "callbackAdmin";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        return new AdminPageBean(abean, model.getPlayer());
    }

}
