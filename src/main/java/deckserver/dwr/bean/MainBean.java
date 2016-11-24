package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.PlayerModel;
import deckserver.dwr.Utils;
import deckserver.util.RefreshInterval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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

    {
        news = new NewsBean[]{
                new NewsBean("https://www.facebook.com/groups/jolstatus/", "JOL Status Facebook Group")
        };
    }

    public MainBean(AdminBean abean, PlayerModel model) {
        init(abean, model);
    }

    public void init(AdminBean abean, PlayerModel model) {
        Collection<GameModel> actives = abean.getActiveGames();
        Collection<SummaryBean> gv = new ArrayList<>();
        Collection<PlSummaryBean> mgv = new ArrayList<>();
        Collection<String> gamenames = new HashSet<>();
        loggedin = model.getPlayer() != null;
        if (loggedin) {
            gamenames.addAll(Arrays.asList(JolAdmin.getInstance().getGames(model.getPlayer())));
            refresh = RefreshInterval.calc(abean.getTimestamp());
        }
        for (GameModel game : actives) {
            if (!model.getChangedGames().contains(game.getName())) continue;
            gv.add(game.getSummaryBean());
            if (gamenames.contains(game.getName()) && (game.isOpen() || game.getPlayers().contains(model.getPlayer())))
                mgv.add(new PlSummaryBean(game, model.getPlayer()));
        }
        games = gv.toArray(new SummaryBean[0]);
        mygames = mgv.toArray(new PlSummaryBean[0]);
        remGames = model.getRemovedGames().toArray(new String[0]);
        chat = model.getChat();
        if (chat.length == 0) chat = null;
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
