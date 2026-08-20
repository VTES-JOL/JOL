package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A player's active/tournament/ousted game lists — extracted out of MainBean
 * so a targeted "just the games" read (MainResource) doesn't have to go
 * through MainBean's constructor, which also calls PlayerModel.getChat() and
 * would silently consume its stateful delta cursor as a side effect.
 */
@Getter
public class GamesSummaryBean {

    private final List<GameStatusBean> games;
    private final List<GameStatusBean> tournament;
    private final List<GameStatusBean> ousted;

    public GamesSummaryBean(String playerName) {
        List<String> games = RegistrationService.getRegisteredGames(playerName).stream()
                .filter(gameName -> RegistrationService.isRegistered(gameName, playerName))
                .filter(GameService::isActive)
                .sorted()
                .toList();
        this.games = games.stream()
                .filter(gameName -> GameService.getSummary(gameName).getPlayers().contains(playerName))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());
        this.tournament = games.stream()
                .filter(JolAdmin::isTournament)
                .filter(gameName -> GameService.getSummary(gameName).getPlayers().contains(playerName))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());
        this.ousted = games.stream()
                .filter(gameName -> !GameService.getSummary(gameName).getPlayers().contains(playerName))
                .map(GameStatusBean::new)
                .collect(Collectors.toList());
    }
}
