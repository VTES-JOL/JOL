package net.deckserver.dwr.bean;

import com.google.common.base.Strings;
import net.deckserver.dwr.model.JolAdmin;

import java.util.Collections;
import java.util.List;
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
    }

    public long getActivePlayerCount() {
        return players.stream().filter(player -> !player.isOusted()).count();
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
