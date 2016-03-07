package deckserver.dwr;

import nbclient.vtesmodel.JolAdminFactory;
import deckserver.rich.*;

public class BugsCreator extends AdminCreator {
	public String getFunction() {
		return "loadbugs";
	}

	public Object createData(AdminBean abean, PlayerModel model) {
		JolAdminFactory admin = abean.getAdmin();
		return admin.getBugs();
	}
}
