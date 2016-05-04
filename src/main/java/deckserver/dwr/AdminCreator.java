package deckserver.dwr;

import deckserver.dwr.bean.AdminPageBean;
import deckserver.rich.AdminBean;
import deckserver.rich.PlayerModel;

public class AdminCreator implements ViewCreator {

	public String getFunction() {
		return "doadmin";
	}

	public Object createData(AdminBean abean, PlayerModel model) {
		return new AdminPageBean(abean, model.getPlayer());
	}

}
