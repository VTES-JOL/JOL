package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MainBean {

    private final List<PlayerGameStatusBean> games;
    private final List<PlayerGameStatusBean> ousted;
    private final List<UserSummaryBean> who;
    private final boolean loggedIn;
    private final List<ChatEntryBean> chat;

    public MainBean(PlayerModel model) {
        JolAdmin jolAdmin = JolAdmin.INSTANCE;
        String playerName = model.getPlayerName();
        loggedIn = model.getPlayerName() != null;
        if (loggedIn) {
            Map<Boolean, List<PlayerGameStatusBean>> allGames = jolAdmin.getGames(playerName).stream()
                    .filter(gameName -> jolAdmin.isRegistered(gameName, playerName))
                    .filter(jolAdmin::isActive)
                    .map(gameName -> new PlayerGameStatusBean(gameName, playerName))
                    .sorted(Comparator.comparing(PlayerGameStatusBean::getGameName))
                    .collect(Collectors.partitioningBy(PlayerGameStatusBean::isOusted));
            this.games = allGames.get(false);
            this.ousted = allGames.get(true);
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
