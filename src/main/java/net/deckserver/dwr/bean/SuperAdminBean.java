package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.ArrayList;
import java.util.List;

public class SuperAdminBean {

    private List<String> players = new ArrayList<>();

    public SuperAdminBean(AdminBean abean, PlayerModel model) {
        JolAdmin admin = JolAdmin.getInstance();
        players = admin.getPlayers();
    }

    public List<String> getPlayers() {
        return players;
    }
}
