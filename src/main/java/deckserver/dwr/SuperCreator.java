package deckserver.dwr;

import deckserver.rich.AdminBean;
import deckserver.rich.GameModel;
import deckserver.rich.PlayerModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SuperCreator implements ViewCreator {

	public String getFunction() {
		// TODO Auto-generated method stub
		return "loadsuper";
	}

	public Object createData(AdminBean abean, PlayerModel model) {
		if(!model.isSuper()) return null;
		List<GameModel> games = abean.getActiveGames();
		Collection<String> res = new ArrayList<String>();
		Iterator<GameModel> it = games.iterator();
		while(it.hasNext()) {
			GameModel g = it.next();
			if(g.isActive()) res.add(g.getName());
		}
		return res.toArray();
	}
}
