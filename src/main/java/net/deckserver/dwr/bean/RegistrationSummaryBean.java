package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegistrationSummaryBean {

    private String gameName;
    private List<PlayerRegistrationBean> registrations = new ArrayList<>();

    public RegistrationSummaryBean(AdminBean abean, String name) {
        JolAdmin admin = JolAdmin.getInstance();
        this.registrations = admin.getPlayers().stream()
                .filter(player -> admin.isInvited(name, player) || admin.isRegistered(name, player))
                .map(player -> new PlayerRegistrationBean(abean, player, name))
                .collect(Collectors.toList());
        this.gameName = name;
    }

    public String getGameName() {
        return gameName;
    }

    public List<PlayerRegistrationBean> getRegistrations() {
        return registrations;
    }
}
