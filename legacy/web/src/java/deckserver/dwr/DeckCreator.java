package deckserver.dwr;

import java.util.*;

import deckserver.dwr.bean.*;
import deckserver.rich.*;

public class DeckCreator implements ViewCreator {

	public String getFunction() {
		return "showDecks";
	}

	public Object createData(AdminBean abean, PlayerModel model) {
		String player = model.getPlayer();
		DeckSummaryBean[] beans = model.getDecks();
		List<GameModel> actives = abean.getActiveGames();
		Collection<DeckSummaryBean> games = new ArrayList<DeckSummaryBean>();
		for(Iterator i = actives.iterator(); i.hasNext();) {
			GameModel game = (GameModel) i.next();
			if(abean.getAdmin().isOpen(game.getName()) &&
			 (abean.getAdmin().isInvited(game.getName(), player) || abean.getAdmin().getOwner(game.getName()).equals(player) || abean.getAdmin().getGameDeck(game.getName(), player) != null)) {
				games.add(new DeckSummaryBean(game,model));
			}
		}
		return new DeckBean(beans,games);
	}

}
