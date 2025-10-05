package net.deckserver.dwr.bean;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.services.RegistrationService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class GameStatusBean {

    private final String name;
    private final String gameStatus;
    private final List<RegistrationStatus> registrations;
    private final Map<String, PlayerStatus> players;
    private final OffsetDateTime created;
    private final String format;
    private final String activePlayer;
    private final String predator;
    private final String prey;
    private final String turn;

    public GameStatusBean(String gameName) {
        JolAdmin admin = JolAdmin.INSTANCE;
        this.name = gameName;
        this.format = admin.getFormat(gameName);
        if (admin.isActive(gameName)) {
            this.gameStatus = "Active";
            registrations = Collections.emptyList();
            this.players = RegistrationService.getPlayers(gameName).stream()
                    .filter(playerName -> !Strings.isNullOrEmpty(playerName))
                    .filter(playerName -> RegistrationService.isRegistered(gameName, playerName))
                    .map(playerName -> new PlayerStatus(gameName, playerName))
                    .collect(Collectors.toMap(PlayerStatus::getPlayerName, Function.identity()));
            JolGame game = admin.getGame(gameName);
            this.activePlayer = game.getActivePlayer();
            this.predator = game.getPredatorOf(activePlayer);
            this.prey = game.getPreyOf(activePlayer);
            this.turn = game.getTurnLabel();
        } else {
            this.gameStatus = "Inviting";
            players = Collections.emptyMap();
            registrations = RegistrationService.getPlayers(gameName).stream()
                    .filter(playerName -> !Strings.isNullOrEmpty(playerName))
                    .map(playerName -> new RegistrationStatus(gameName, playerName))
                    .collect(Collectors.toList());
            this.activePlayer = null;
            this.predator = null;
            this.prey = null;
            this.turn = null;
        }
        created = admin.getCreatedTime(gameName);
    }

    public long getActivePlayerCount() {
        return players.values().stream().filter(player -> !player.isOusted()).count();
    }

    public String getCreated() {
        return Optional.ofNullable(created)
                .map(value -> value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(null);
    }

}
