package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.PlayerModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AllGamesBean {

    private List<SummaryBean> games;

    public AllGamesBean(AdminBean abean, PlayerModel model) {
        this.games = abean.getActiveGames()
                .stream()
                .map(GameModel::getSummaryBean)
                .collect(Collectors.toList());
        games.sort(Comparator.comparing(SummaryBean::getGame));
    }

    public List<SummaryBean> getGames() {
        return games;
    }
}
