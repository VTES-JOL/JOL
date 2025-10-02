package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AdminPageBean {

    private final List<UserSummaryBean> userRoles;
    private final List<PlayerActivityStatus> players;
    private final List<String> substitutes;
    private final List<String> games;
    private final List<GameActivityStatus> idleGames;

    public AdminPageBean(PlayerModel model) {
        JolAdmin admin = JolAdmin.INSTANCE;
        List<String> currentPlayers = admin.getPlayers().stream().sorted().toList();
        List<PlayerActivityStatus> playerActivityStatuses = currentPlayers.stream()
                .map(PlayerActivityStatus::new)
                .sorted(Comparator.comparing(PlayerActivityStatus::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        OffsetDateTime currentYear = OffsetDateTime.now().minusYears(1);
        OffsetDateTime currentMonth = OffsetDateTime.now().minusMonths(1);
        this.userRoles = currentPlayers.stream()
                .map(UserSummaryBean::new)
                .filter(UserSummaryBean::isSpecialUser)
                .sorted(Comparator.comparing(UserSummaryBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        this.players = playerActivityStatuses.stream()
                .filter(playerActivityStatus -> playerActivityStatus.online().isBefore(currentYear))
                .filter(playerActivityStatus -> playerActivityStatus.getLegacyDeckCount() + playerActivityStatus.getModernDeckCount() < 5)
                .filter(playerActivityStatus -> playerActivityStatus.getActiveGamesCount() == 0)
                .sorted(Comparator.comparing(PlayerActivityStatus::getLastOnline))
                .collect(Collectors.toList());
        this.substitutes = playerActivityStatuses.stream()
                .filter(playerActivityStatus -> playerActivityStatus.online().isAfter(currentMonth))
                .map(PlayerActivityStatus::getName)
                .collect(Collectors.toList());
        this.games = admin.getGames().stream()
                .sorted()
                .filter(admin::isActive)
                .collect(Collectors.toList());
        this.idleGames = this.games.stream()
                .map(GameActivityStatus::new)
                .filter(gameActivityStatus -> gameActivityStatus.timestamp().isBefore(currentMonth))
                .sorted(Comparator.comparing(GameActivityStatus::timestamp))
                .collect(Collectors.toList());
    }
}
