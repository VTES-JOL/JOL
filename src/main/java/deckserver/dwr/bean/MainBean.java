package deckserver.dwr.bean;

import deckserver.dwr.Utils;
import deckserver.rich.AdminBean;
import deckserver.rich.GameModel;
import deckserver.rich.PlayerModel;
import deckserver.util.RefreshInterval;
import nbclient.vtesmodel.JolAdminFactory;

import java.util.*;

public class MainBean {

	private PlSummaryBean[] mygames = null;
	private String[] who = null;
	private String[] admins = null;
	private boolean loggedin;
	private SummaryBean[] games;
	private String[] chat;
	private int refresh = 0;
	private NewsBean[] news;
	private String stamp;
	private String[] remGames;

	public MainBean(AdminBean abean, PlayerModel model) {
		init(abean,model);
	}
	
	public void init(AdminBean abean, PlayerModel model) {
		Collection<GameModel> actives = abean.getActiveGames();
		Collection<SummaryBean> gv = new ArrayList<SummaryBean>();
		Collection<PlSummaryBean> mgv = new ArrayList<PlSummaryBean>();
		Collection<String> gamenames = new HashSet<String>();
		loggedin = model.getPlayer() != null;
		if(loggedin) {
			gamenames.addAll(Arrays.asList(JolAdminFactory.INSTANCE.getGames(model.getPlayer())));
			refresh = RefreshInterval.calc(abean.getTimestamp());
		}
		for(Iterator i = actives.iterator(); i.hasNext();) {
			GameModel game = (GameModel) i.next();
			if(!model.getChangedGames().contains(game.getName())) continue;
			gv.add(game.getSummaryBean());
			if(gamenames.contains(game.getName()) && (game.isOpen() || game.getPlayers().contains(model.getPlayer())))
				mgv.add(new PlSummaryBean(game,model.getPlayer()));
		}
		games = (SummaryBean[]) gv.toArray(new SummaryBean[0]);
		mygames = (PlSummaryBean[]) mgv.toArray(new PlSummaryBean[0]);
		remGames = model.getRemovedGames().toArray(new String[0]);
		chat = model.getChat();
		if(chat.length == 0) chat = null;
		who = abean.getWho();
		admins = abean.getAdmins();
		stamp = Utils.getDate();
		model.clearGames();
	}
	
	public String getStamp() {
		return stamp;
	}
	
	public SummaryBean[] getGames() {
		return games;
	}
	
	public String[] getWho() {
		return who;
	}
	
	public String[] getAdmins() {
		return admins;
	}
	
	public String[] getChat() {
		return chat;
	}
	
	public int getRefresh() {
		return refresh;
	}
	
	{ news = new NewsBean[] {
                        new NewsBean("/tourney/kjm2010","KevinM's 2010 tourney"),
			new NewsBean("http://groups.google.com/group/jol-league","Join the JOL League."),
			new NewsBean("http://www.white-wolf.com/vtes/","Visit VtES home page")
	    };
	}
	
	public NewsBean[] getNews() {
		return news;
	}
	
	public boolean isLoggedIn() {
		return loggedin;
	}
	
	public PlSummaryBean[] getMyGames() {
		return mygames;
	}
	
	public String[] getRemGames() {
		return remGames;
	}

}
