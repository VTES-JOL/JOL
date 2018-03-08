package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.List;
import java.util.stream.Collectors;

public class SuperAdminBean {

    private List<String> players;
    private List<String> admins;
    private List<String> judges;

    public SuperAdminBean(AdminBean abean, PlayerModel model) {
        JolAdmin admin = JolAdmin.getInstance();
        players = admin.getPlayers();
        admins = players.stream()
                .filter(admin::isAdmin)
                .sorted().collect(Collectors.toList());
        judges = players.stream().filter(admin::isJudge).sorted().collect(Collectors.toList());
    }

    public List<String> getPlayers() {
        return players;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public List<String> getJudges() {
        return judges;
    }
}
