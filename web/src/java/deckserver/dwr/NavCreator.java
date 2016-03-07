package deckserver.dwr;

import deckserver.dwr.bean.NavBean;
import deckserver.rich.*;

public class NavCreator implements ViewCreator {

	public String getFunction() {
		return "navigate";
	}

	public Object createData(AdminBean abean, PlayerModel model) {
		return new NavBean(abean, model);
	}

}
