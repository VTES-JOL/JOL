package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.NavBean;
import net.deckserver.dwr.model.PlayerModel;

class NavCreator implements ViewCreator {

    public String getFunction() {
        return "navigate";
    }

    public Object createData(PlayerModel model) {
        return new NavBean(model);
    }

}
