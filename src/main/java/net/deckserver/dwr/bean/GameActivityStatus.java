package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.services.RegistrationService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class GameActivityStatus {

    private final String gameName;
    private final Map<String, String> idlePlayers = new HashMap<>();
    private final OffsetDateTime gameTimestamp;

    public GameActivityStatus(String gameName) {
        this.gameName = gameName;
        gameTimestamp = JolAdmin.getGameTimeStamp(gameName);
        Set<String> players = RegistrationService.getPlayers(gameName);
        players.forEach(player -> {
            OffsetDateTime playerTimestamp = JolAdmin.getPlayerAccess(player, gameName);
            idlePlayers.put(player, playerTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        });
    }

    public OffsetDateTime timestamp() {
        return gameTimestamp;
    }

    public String getGameTimestamp() {
        return gameTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
