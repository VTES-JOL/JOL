package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.storage.json.system.GameHistory;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class AllGamesBean {

    private final List<GameSummaryBean> games;
    private final List<GameHistory> history;

    public AllGamesBean(PlayerModel model) {
        this.games = JolAdmin.INSTANCE.getGames().stream()
                .filter(JolAdmin.INSTANCE::isActive)
                .map(GameSummaryBean::new)
                .collect(Collectors.toList());
        games.sort(Comparator.comparing(GameSummaryBean::getGameName, String.CASE_INSENSITIVE_ORDER));

        this.history = JolAdmin.INSTANCE.getHistory().entrySet().stream()
                .sorted(Map.Entry.<OffsetDateTime, GameHistory>comparingByKey().reversed())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
