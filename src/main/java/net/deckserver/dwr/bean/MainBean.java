package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.*;
import java.util.stream.Collectors;

public class MainBean {

    private final List<PlayerGameStatusBean> games;
    private final List<UserSummaryBean> who;
    private final boolean loggedIn;
    private final List<ChatEntryBean> chat;
    private final String stamp;

    public MainBean(PlayerModel model) {
        JolAdmin jolAdmin = JolAdmin.getInstance();
        String playerName = model.getPlayer();
        loggedIn = model.getPlayer() != null;
        if (loggedIn) {
            this.games = Arrays.stream(jolAdmin.getGames())
                    .filter(Objects::nonNull)
                    .filter(jolAdmin::isActive)
                    .filter(gameName -> jolAdmin.isRegistered(gameName, playerName))
                    .map(gameName -> new PlayerGameStatusBean(gameName, playerName))
                    .sorted(Comparator.comparing(PlayerGameStatusBean::isOusted).thenComparing(PlayerGameStatusBean::getGameName))
                    .collect(Collectors.toList());
            chat = model.getChat();
            who = JolAdmin.getInstance().getWho().stream()
                    .sorted(Comparator.reverseOrder())
                    .map(UserSummaryBean::new)
                    .collect(Collectors.toList());
        } else {
            this.games = Collections.emptyList();
            this.chat = Collections.emptyList();
            this.who = Collections.emptyList();
        }

        stamp = JolAdmin.getDate();
    }

    public List<PlayerGameStatusBean> getGames() {
        return games;
    }

    public List<UserSummaryBean> getWho() {
        return who;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public List<ChatEntryBean> getChat() {
        return chat;
    }

    public String getStamp() {
        return stamp;
    }

}
