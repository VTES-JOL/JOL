package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.system.UserSummary;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MainBean {

    private final List<GameStatusBean> games;
    private final List<GameStatusBean> ousted;
    private final List<UserSummary> who;
    private final boolean loggedIn;
    private final List<ChatEntryBean> chat;

    public MainBean(PlayerModel model) {
        String playerName = model.getPlayerName();
        loggedIn = model.getPlayerName() != null;
        if (loggedIn) {
            List<String> games = RegistrationService.getRegisteredGames(playerName).stream()
                    .filter(gameName -> RegistrationService.isRegistered(gameName, playerName))
                    .filter(GameService::isActive)
                    .sorted()
                    .toList();
            this.games = games.stream()
                    .filter(gameName -> GameService.getSummary(gameName).getPlayers().contains(playerName))
                    .map(GameStatusBean::new)
                    .collect(Collectors.toList());
            this.ousted = games.stream()
                    .filter(gameName -> !GameService.getSummary(gameName).getPlayers().contains(playerName))
                    .map(GameStatusBean::new)
                    .collect(Collectors.toList());
            chat = model.getChat();
            who = PlayerService.activeUsers();
        } else {
            this.games = Collections.emptyList();
            this.ousted = Collections.emptyList();
            this.chat = Collections.emptyList();
            this.who = Collections.emptyList();
        }
    }

}
