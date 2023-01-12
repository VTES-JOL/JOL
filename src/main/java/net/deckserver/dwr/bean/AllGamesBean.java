package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AllGamesBean {

    private List<GameSummaryBean> games;

    public AllGamesBean(PlayerModel model) {
        this.games = Arrays.stream(JolAdmin.getInstance().getGames())
                .filter(Objects::nonNull)
                .filter(JolAdmin.getInstance()::isActive)
                .map(GameSummaryBean::new)
                .collect(Collectors.toList());
        games.sort(Comparator.comparing(GameSummaryBean::getGameName));
    }

    public List<GameSummaryBean> getGames() {
        return games;
    }
}
