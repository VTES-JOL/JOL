package net.deckserver.rest.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RegistrationService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class PlayerActivityStatus {
    private final String name;
    private final OffsetDateTime lastOnline;
    private final Long legacyDeckCount;
    private final Long modernDeckCount;
    private final Integer activeGamesCount;

    public PlayerActivityStatus(String name) {
        this.name = name;
        this.lastOnline = JolAdmin.getPlayerAccess(name);
        Map<DeckFormat, Long> collect = DeckService.getPlayerDeckNames(name)
                .stream()
                .collect(Collectors.groupingBy(deckName -> JolAdmin.getDeckFormat(name, deckName), () -> new EnumMap<>(DeckFormat.class), Collectors.counting()));
        legacyDeckCount = Optional.ofNullable(collect.get(DeckFormat.LEGACY)).orElse(0L);
        modernDeckCount = Optional.ofNullable(collect.get(DeckFormat.MODERN)).orElse(0L);
        activeGamesCount = RegistrationService.getPlayerGames(name).size();
    }

    @JsonIgnore
    public OffsetDateTime online() {
        return lastOnline;
    }

    public String getLastOnline() {
        return lastOnline.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /** Names of players seen within the last month, most-recently-online first — shared by admin/lobby "who's around" pickers. */
    public static List<String> recentlyActiveNames() {
        OffsetDateTime currentMonth = OffsetDateTime.now().minusMonths(1);
        return PlayerService.getPlayers().stream()
                .sorted()
                .map(PlayerActivityStatus::new)
                .filter(status -> status.online().isAfter(currentMonth))
                .sorted(Comparator.comparing(PlayerActivityStatus::getLastOnline))
                .map(PlayerActivityStatus::getName)
                .toList();
    }
}
