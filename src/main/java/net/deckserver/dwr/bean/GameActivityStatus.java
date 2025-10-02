package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;

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
        JolAdmin admin = JolAdmin.INSTANCE;
        this.gameName = gameName;
        gameTimestamp = admin.getGameTimeStamp(gameName);
        Set<String> players = admin.getPlayers(gameName);
        players.forEach(player -> {
            OffsetDateTime playerTimestamp = admin.getPlayerAccess(player, gameName);
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
