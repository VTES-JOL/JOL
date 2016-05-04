package deckserver.dwr;

import deckserver.rich.AdminBean;
import deckserver.rich.PlayerModel;

public interface ViewCreator {

    public String getFunction();

    public Object createData(AdminBean abean, PlayerModel model);

}
