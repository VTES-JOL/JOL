package deckserver.dwr;

import deckserver.rich.AdminBean;
import deckserver.rich.PlayerModel;
import nbclient.vtesmodel.JolAdminFactory;

public class BugsCreator extends AdminCreator {
	public String getFunction() {
		return "loadbugs";
	}

	public Object createData(AdminBean abean, PlayerModel model) {
		JolAdminFactory admin = abean.getAdmin();
		return admin.getBugs();
	}
}
