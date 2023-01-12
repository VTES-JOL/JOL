package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AllGamesBean {

    private List<GameSummaryBean> games;

    public AllGamesBean(PlayerModel model) {
        this.games = JolAdmin.getInstance().getActiveGames()
                .stream()
                .map(GameModel::getSummaryBean)
                .collect(Collectors.toList());
        games.sort(Comparator.comparing(GameSummaryBean::getGame));
    }

    public List<GameSummaryBean> getGames() {
        return games;
    }
}
