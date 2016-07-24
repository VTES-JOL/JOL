package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.MainBean;

public class MainCreator implements ViewCreator {

    public String getFunction() {
        return "callbackMain";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        return new MainBean(abean, model);
    }

}
