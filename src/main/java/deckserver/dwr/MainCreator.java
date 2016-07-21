package deckserver.dwr;

import deckserver.dwr.bean.MainBean;
import deckserver.dwr.bean.AdminBean;

public class MainCreator implements ViewCreator {

    public String getFunction() {
        return "loadMain";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        return new MainBean(abean, model);
    }

}
