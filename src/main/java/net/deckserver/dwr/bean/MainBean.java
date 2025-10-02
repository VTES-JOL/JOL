package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MainBean {

    private final List<GameStatusBean> games;
    private final List<GameStatusBean> ousted;
    private final List<UserSummaryBean> who;
    private final boolean loggedIn;
    private final List<ChatEntryBean> chat;

    public MainBean(PlayerModel model) {
        JolAdmin jolAdmin = JolAdmin.INSTANCE;
        String playerName = model.getPlayerName();
        loggedIn = model.getPlayerName() != null;
        if (loggedIn) {
            List<GameStatusBean> games = jolAdmin.getGames(playerName).stream()
                    .filter(gameName -> jolAdmin.isRegistered(gameName, playerName))
                    .filter(jolAdmin::isActive)
                    .map(GameStatusBean::new)
                    .sorted(Comparator.comparing(GameStatusBean::getName))
                    .toList();
            this.games = games.stream()
                    .filter(game -> jolAdmin.isAlive(game.getName(), playerName))
                    .collect(Collectors.toList());
            this.ousted = games.stream()
                    .filter(game -> !jolAdmin.isAlive(game.getName(), playerName))
                    .collect(Collectors.toList());
            chat = model.getChat();
            who = JolAdmin.INSTANCE.getWho().stream()
                    .sorted(Comparator.reverseOrder())
                    .map(UserSummaryBean::new)
                    .collect(Collectors.toList());
        } else {
            this.games = Collections.emptyList();
            this.ousted = Collections.emptyList();
            this.chat = Collections.emptyList();
            this.who = Collections.emptyList();
        }
    }

}
