package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.MainBean;
import net.deckserver.dwr.model.PlayerModel;

class MainCreator implements ViewCreator {

    public String getFunction() {
        return "callbackMain";
    }

    public Object createData(PlayerModel model) {
        return new MainBean(model);
    }

}
