package net.deckserver.rest.bean;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.model.JolGame;
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
    // Last-activity time (registration, invite, …), not creation time — this is
    // what GameCleanUp measures its 5-day stale-lobby window against, so the
    // client's "closes in N days" label is derived from it.
    private final OffsetDateTime updated;
    private final String format;
    private final String activePlayer;
    private final String predator;
    private final String prey;
    private final String turn;
    /** Round number (integer before the dot in the raw turn); 0 for a game that hasn't started a turn / isn't active. */
    private final int round;
    /** Player holding the edge, or null. */
    private final String edge;
    /** Player names in seating order — {@link #players} is an unordered map, this preserves the table order. */
    private final List<String> seating;
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
            this.round = parseRound(game.getCurrentTurn());
            this.edge = game.getEdge();
            this.seating = List.copyOf(game.getPlayers());
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
            this.round = 0;
            this.edge = null;
            this.seating = Collections.emptyList();
        }
        updated = JolAdmin.getUpdatedTime(gameName);
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

    private static int parseRound(String rawTurn) {
        try {
            return Integer.parseInt(rawTurn.split("\\.")[0]);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public String getUpdated() {
        return Optional.ofNullable(updated)
                .map(value -> value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(null);
    }

}
