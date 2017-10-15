package deckserver.dwr.bean;

import deckserver.client.JolAdmin;

import java.util.*;

public class AdminPageBean {

    private List<String> players = new ArrayList<>();
    private List<String> currentGames = new ArrayList<>();
    private List<RegistrationSummaryBean> forming = new ArrayList<>();

    public AdminPageBean(AdminBean abean, String player) {
        JolAdmin admin = JolAdmin.getInstance();
        List<String> games = Arrays.asList(admin.getGames());
        games.stream()
                .filter(Objects::nonNull)
                .filter(gameName -> admin.isSuperUser(player) || admin.getOwner(gameName).equals(player))
                .forEach(gameName -> {
                    if (admin.isOpen(gameName)) {
                        forming.add(new RegistrationSummaryBean(abean, gameName));
                    } else if (!admin.isFinished(gameName)) {
                        currentGames.add(gameName);
                    }
                });
        players = admin.getPlayers();
        Collections.sort(players);
        Collections.sort(currentGames);
    }

    public List<String> getPlayers() {
        return players;
    }

    public List<String> getCurrentGames() {
        return currentGames;
    }

    public List<RegistrationSummaryBean> getForming() {
        return forming;
    }

}
