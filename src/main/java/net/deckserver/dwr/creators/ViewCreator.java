package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.model.PlayerModel;

interface ViewCreator {

    String getFunction();

    Object createData(AdminBean abean, PlayerModel model);

}
