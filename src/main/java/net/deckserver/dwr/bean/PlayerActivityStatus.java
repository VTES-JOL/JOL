package net.deckserver.dwr.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.RegistrationService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
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
        JolAdmin admin = JolAdmin.INSTANCE;
        this.name = name;
        this.lastOnline = admin.getPlayerAccess(name);
        Map<DeckFormat, Long> collect = DeckService.getPlayerDeckNames(name)
                .stream()
                .collect(Collectors.groupingBy(deckName -> admin.getDeckFormat(name, deckName), () -> new EnumMap<>(DeckFormat.class), Collectors.counting()));
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
}
