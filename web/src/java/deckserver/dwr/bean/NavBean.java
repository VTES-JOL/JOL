package deckserver.dwr.bean;

import java.util.*;

import nbclient.vtesmodel.JolAdminFactory;

import deckserver.rich.*;

public class NavBean {
	
	private static final Map<String,String> loggedIn = new HashMap<String,String>();
	private static final Map<String,String> hasChats;
	private static final Map<String,String> loggedOut = new HashMap<String,String>();
	private static final Map<String,String> noadmin = new HashMap<String,String>();
	private static final Map<String,String> isadmin = new HashMap<String,String>();
	private static final Map<String,String> suser = new HashMap<String,String>();
	
	static {
		loggedOut.put("main","Main");
		loggedIn.put("main","Main");
		loggedIn.put("deck","Deck Register");
	//	loggedIn.put("bugs", "Bugs");
		isadmin.put("admin", "Game Admin");
		suser.put("admin", "Game Admin");
		suser.put("suser", "Site Admin");
		hasChats = new HashMap<String,String>(loggedIn);
		hasChats.put("main", "Main *");
	}
	
	private Map<String,String> gameB = new HashMap<String,String>(),playerB = loggedOut,adminB = noadmin;
	String player,game = null,target;
	
	public NavBean(AdminBean abean, PlayerModel model) {
		player = model.getPlayer();
		target = model.getView();
		if(target.equals("game")) 
			game = model.getCurrentGame();
		if(player != null) {
			playerB = model.hasChats() ? hasChats : loggedIn;
			JolAdminFactory admin = JolAdminFactory.INSTANCE;
			if(admin.isSuperUser(player)) {
				adminB = suser;
			} else if (admin.isAdmin(player)) {
				adminB = isadmin;
			}
		}
		String[] games = model.getCurrentGames();
		for(int i = 0; i < games.length; i++) {
			GameModel gmodel = abean.getGameModel(games[i]);
			GameView view = gmodel.getView(player);
			String current = view.isChanged() ? " *" : "";
			gameB.put("g" + games[i], games[i] + current);
		}
	}
	
	public Map<String,String> getGameButtons() {
		return gameB;
	}
	
	public Map<String,String> getPlayerButtons() {
		return playerB;
	}
	
	public Map<String,String> getAdminButtons() {
		return adminB;
	}
	
	public String getPlayer() {
		return player;
	}
	
	public String getGame() {
		return game;
	}
	
	public String getTarget() {
		return target;
	}
	
}
