package net.deckserver.dwr.bean;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.services.GameService;
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
    private final String gameId;
    private final String gameStatus;
    private final List<RegistrationStatus> registrations;
    private final Map<String, PlayerStatus> players;
    private final OffsetDateTime created;
    private final String format;
    private final String activePlayer;
    private final String predator;
    private final String prey;
    private final String turn;
    private final String visibility;
    private final String owner;
    private final String playerRelationship;

    public GameStatusBean(String gameName) {
        this(gameName, null);
    }

    public GameStatusBean(String gameName, String playerName) {
        this.name = gameName;
        this.gameId = GameService.get(gameName).getId();
        this.format = JolAdmin.getFormat(gameName);
        this.owner = JolAdmin.getOwner(gameName);
        this.visibility = JolAdmin.isPublic(gameName) ? "PUBLIC" : "PRIVATE";
        if (JolAdmin.isActive(gameName)) {
            this.gameStatus = "Active";
            registrations = Collections.emptyList();
            this.players = RegistrationService.getPlayers(gameName).stream()
                    .filter(p -> !Strings.isNullOrEmpty(p))
                    .filter(p -> RegistrationService.isRegistered(gameName, p))
                    .map(p -> new PlayerStatus(gameName, p))
                    .collect(Collectors.toMap(PlayerStatus::getPlayerName, Function.identity()));
            JolGame game = GameService.getGameByName(gameName);
            this.activePlayer = game.getActivePlayer();
            this.predator = game.getPredatorOf(activePlayer);
            this.prey = game.getPreyOf(activePlayer);
            this.turn = game.getTurnLabel();
        } else {
            this.gameStatus = "Inviting";
            players = Collections.emptyMap();
            registrations = RegistrationService.getPlayers(gameName).stream()
                    .filter(p -> !Strings.isNullOrEmpty(p))
                    .map(p -> new RegistrationStatus(gameName, p))
                    .collect(Collectors.toList());
            this.activePlayer = null;
            this.predator = null;
            this.prey = null;
            this.turn = null;
        }
        created = JolAdmin.getCreatedTime(gameName);
        if (playerName == null) {
            this.playerRelationship = null;
        } else if (this.owner.equals(playerName)) {
            this.playerRelationship = "OWNER";
        } else if (RegistrationService.isRegistered(gameName, playerName)) {
            this.playerRelationship = "REGISTERED";
        } else if (RegistrationService.isInGame(gameName, playerName)) {
            this.playerRelationship = "INVITED";
        } else {
            this.playerRelationship = "OPEN";
        }
    }

    public String getCreated() {
        return Optional.ofNullable(created)
                .map(value -> value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(null);
    }

}
