package net.deckserver.dwr.bean;

import com.google.common.base.Strings;
import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameStatusBean {

    @Getter
    private final String name;
    @Getter
    private final String gameStatus;
    @Getter
    private final List<PlayerRegistrationStatusBean> registrations;
    @Getter
    private final List<PlayerGameStatusBean> players;
    private final OffsetDateTime created;

    public GameStatusBean(String gameName) {
        JolAdmin admin = JolAdmin.getInstance();
        this.name = gameName;
        if (admin.isActive(gameName)) {
            this.gameStatus = "Active";
            registrations = Collections.emptyList();
            players = admin.getPlayers(gameName).stream()
                    .filter(playerName -> !Strings.isNullOrEmpty(playerName))
                    .filter(playerName -> admin.isRegistered(gameName, playerName))
                    .map(playerName -> new PlayerGameStatusBean(gameName, playerName))
                    .collect(Collectors.toList());
        } else {
            this.gameStatus = "Inviting";
            players = Collections.emptyList();
            registrations = admin.getPlayers(gameName).stream()
                    .filter(playerName -> !Strings.isNullOrEmpty(playerName))
                    .map(playerName -> new PlayerRegistrationStatusBean(gameName, playerName))
                    .collect(Collectors.toList());
        }
        created = admin.getCreatedTime(gameName);
    }

    public long getActivePlayerCount() {
        return players.stream().filter(player -> !player.isOusted()).count();
    }

    public String getCreated() {
        return created.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
