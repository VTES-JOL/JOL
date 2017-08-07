package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.PlayerModel;
import deckserver.dwr.Utils;
import deckserver.util.RefreshInterval;

import java.util.*;

public class MainBean {

    private List<PlSummaryBean> mygames = new ArrayList<>();
    private String[] who = null;
    private String[] admins = null;
    private boolean loggedin;
    private List<SummaryBean> games = new ArrayList<>();
    private String[] chat;
    private int refresh = 0;
    private String stamp;
    private String[] remGames;
    private String message;

    public MainBean(AdminBean abean, PlayerModel model) {
        init(abean, model);
    }

    public void init(AdminBean abean, PlayerModel model) {
        Collection<GameModel> actives = abean.getActiveGames();
        Collection<String> gamenames = new HashSet<>();
        loggedin = model.getPlayer() != null;
        if (loggedin) {
            gamenames.addAll(Arrays.asList(JolAdmin.getInstance().getGames(model.getPlayer())));
            refresh = RefreshInterval.calc(abean.getTimestamp());
        }
        for (GameModel game : actives) {
            if (!model.getChangedGames().contains(game.getName())) continue;
            games.add(game.getSummaryBean());
            if (gamenames.contains(game.getName()) && (game.isOpen() || game.getPlayers().contains(model.getPlayer())))
                mygames.add(new PlSummaryBean(game, model.getPlayer()));
        }
        remGames = model.getRemovedGames().toArray(new String[0]);
        chat = model.getChat();
        if (chat.length == 0) chat = null;
        who = abean.getWho();
        admins = abean.getAdmins();
        stamp = Utils.getDate();
        message = abean.getMessage();
        mygames.sort(Comparator.comparing(PlSummaryBean::getGame));
        games.sort(Comparator.comparing(SummaryBean::getGame));
        model.clearGames();
    }

    public String getStamp() {
        return stamp;
    }

    public List<SummaryBean> getGames() {
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

    public String getMessage() {
        return message;
    }

    public boolean isLoggedIn() {
        return loggedin;
    }

    public List<PlSummaryBean> getMyGames() {
        return mygames;
    }

    public String[] getRemGames() {
        return remGames;
    }

}
