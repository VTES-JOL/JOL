package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;

interface ViewCreator {

    String getFunction();

    Object createData(AdminBean abean, PlayerModel model);

}
