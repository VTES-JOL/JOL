package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;

public interface ViewCreator {

    String getFunction();

    Object createData(AdminBean abean, PlayerModel model);

}
