package deckserver.dwr;

import deckserver.rich.*;

public interface ViewCreator {

	public String getFunction();

	public Object createData(AdminBean abean, PlayerModel model);
	
}
