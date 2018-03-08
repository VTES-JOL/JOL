package net.deckserver.dwr.bean;

import net.deckserver.Utils;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.*;
import java.util.stream.Collectors;

public class MainBean {

    private List<PlayerSummaryBean> myGames = new ArrayList<>();
    private List<UserSummaryBean> who = new ArrayList<>();
    private boolean loggedIn;
    private List<SummaryBean> games = new ArrayList<>();
    private List<ChatEntryBean> chat;
    private int refresh = 0;
    private String stamp;
    private List<String> removedGames = new ArrayList<>();
    private String message;

    public MainBean(AdminBean abean, PlayerModel model) {
        init(abean, model);
    }

    public void init(AdminBean abean, PlayerModel model) {
        JolAdmin jolAdmin = JolAdmin.getInstance();
        Collection<GameModel> actives = abean.getActiveGames();
        Collection<String> gamenames = new HashSet<>();
        loggedIn = model.getPlayer() != null;
        if (loggedIn) {
            gamenames.addAll(Arrays.asList(JolAdmin.getInstance().getGames(model.getPlayer())));
            refresh = Utils.calc(abean.getTimestamp());
        }
        for (GameModel game : actives) {
            if (!model.getChangedGames().contains(game.getName())) continue;
            games.add(game.getSummaryBean());
            if (gamenames.contains(game.getName()) && (game.isOpen() || game.getPlayers().contains(model.getPlayer())))
                myGames.add(new PlayerSummaryBean(game, model.getPlayer()));
        }
        removedGames = new ArrayList<>(model.getRemovedGames());
        chat = model.getChat();
        who = abean.getWho().stream()
                .sorted(Comparator.reverseOrder())
                .map(who -> new UserSummaryBean(who, jolAdmin.isAdmin(who), jolAdmin.isSuperUser(who), jolAdmin.isJudge(who)))
                .collect(Collectors.toList());
        stamp = JolAdmin.getDate();
        message = abean.getMessage();
        myGames.sort(Comparator.comparing(PlayerSummaryBean::getGame));
        games.sort(Comparator.comparing(SummaryBean::getGame));
        model.clearGames();
    }

    public List<PlayerSummaryBean> getMyGames() {
        return myGames;
    }

    public List<UserSummaryBean> getWho() {
        return who;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public List<SummaryBean> getGames() {
        return games;
    }

    public List<ChatEntryBean> getChat() {
        return chat;
    }

    public int getRefresh() {
        return refresh;
    }

    public String getStamp() {
        return stamp;
    }

    public List<String> getRemovedGames() {
        return removedGames;
    }

    public String getMessage() {
        return message;
    }
}
