package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class AdminPageBean {

    private final List<UserSummaryBean> userRoles;
    private final List<PlayerActivityStatus> players;
    private final List<String> substitutes;
    private final List<String> games;
    private final List<GameActivityStatus> idleGames;

    public AdminPageBean(PlayerModel model) {
        JolAdmin admin = JolAdmin.getInstance();
        List<String> currentPlayers = admin.getPlayers().stream().sorted().collect(Collectors.toList());
        List<PlayerActivityStatus> playerActivityStatuses = currentPlayers.stream()
                .map(PlayerActivityStatus::new)
                .sorted(Comparator.comparing(PlayerActivityStatus::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        OffsetDateTime currentYear = OffsetDateTime.of(OffsetDateTime.now().getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        this.userRoles = currentPlayers.stream()
                .map(UserSummaryBean::new)
                .filter(UserSummaryBean::isSpecialUser)
                .sorted(Comparator.comparing(UserSummaryBean::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        this.players = playerActivityStatuses.stream()
                .filter(playerActivityStatus -> playerActivityStatus.online().isBefore(currentYear))
                .filter(playerActivityStatus -> playerActivityStatus.getLegacyDeckCount() + playerActivityStatus.getModernDeckCount() < 5)
                .filter(playerActivityStatus -> playerActivityStatus.getActiveGamesCount() == 0)
                .collect(Collectors.toList());
        this.substitutes = playerActivityStatuses.stream()
                .filter(playerActivityStatus -> playerActivityStatus.online().isAfter(currentYear))
                .map(PlayerActivityStatus::getName)
                .collect(Collectors.toList());
        this.games = admin.getGames().stream()
                .sorted()
                .filter(admin::isActive)
                .collect(Collectors.toList());
        this.idleGames = this.games.stream()
                .map(GameActivityStatus::new)
                .filter(gameActivityStatus -> gameActivityStatus.timestamp().isBefore(OffsetDateTime.now().minusMonths(1)))
                .sorted(Comparator.comparing(GameActivityStatus::timestamp))
                .collect(Collectors.toList());
    }
}
