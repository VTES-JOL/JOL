package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.JolGame;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameStatusBean {

    private final String name;
    private final String gameStatus;
    private final List<PlayerRegistrationStatusBean> registrations;
    private final List<PlayerGameStatusBean> players;

    public GameStatusBean(String gameName) {
        JolAdmin admin = JolAdmin.getInstance();
        this.name = gameName;
        if (admin.isActive(gameName)) {
            this.gameStatus = "Active";
            registrations = Collections.emptyList();
            players = Arrays.stream(admin.getPlayers(gameName))
                    .filter(Objects::nonNull)
                    .filter(player -> admin.isRegistered(gameName, player))
                    .map(playerName -> new PlayerGameStatusBean(gameName, playerName))
                    .collect(Collectors.toList());
        } else {
            this.gameStatus = "Inviting";
            players = Collections.emptyList();
            registrations = admin.getPlayers().stream()
                    .filter(Objects::nonNull)
                    .filter(player -> admin.isInvited(gameName, player) || admin.isRegistered(gameName, player))
                    .map(playerName -> new PlayerRegistrationStatusBean(gameName, playerName))
                    .collect(Collectors.toList());
        }
    }

    public String getName() {
        return name;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public List<PlayerRegistrationStatusBean> getRegistrations() {
        return registrations;
    }

    public List<PlayerGameStatusBean> getPlayers() {
        return players;
    }
}
