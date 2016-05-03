package deckserver.dwr.bean;

import java.text.*;
import java.util.*;

import nbclient.vtesmodel.JolAdminFactory;

import deckserver.rich.*;

public class SummaryBean {

	private String game;

	private String access = "none";

	private String turn = null;

	private String[] available = new String[0];

	private static DateFormat format = new SimpleDateFormat("HH:mm M/d ");
    private String getDate(long timestamp) {
        return format.format(new Date(timestamp));
    }
    
    public SummaryBean(GameModel game) {
		this.game = game.getName();
		if (JolAdminFactory.INSTANCE.isActive(this.game)) {
			access = getDate(game.getTimestamp());
			turn = JolAdminFactory.INSTANCE.getGame(this.game).getCurrentTurn();
			GameView[] views = game.getViews();
			Collection<String> actives = new ArrayList<String>(5);
			for (int i = 0; i < views.length; i++) {
				if(views[i].isPlayer()) actives.add(views[i].getPlayer());
			}
			available = actives.toArray(new String[0]);
		}
	}

	public String getAccess() {
		return access;
	}

	public String[] getAvailable() {
		return available;
	}

	public String getGame() {
		return game;
	}

	public String getTurn() {
		return turn;
	}

}
