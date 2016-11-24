package deckserver.dwr.bean;

import deckserver.client.JolAdmin;

import java.util.*;

public class AdminPageBean {

    // map of Game name -> RegistrationSummaryBean[]
    private Map<String, RegistrationSummaryBean> games = new HashMap<>();
    private String[] players;
    private String[] rGames;

    public AdminPageBean(AdminBean abean, String player) {
        JolAdmin admin = JolAdmin.getInstance();
        String[] names = admin.getGames(player);
        Collection<String> c = new ArrayList<>();
        for (String gameName : names) {
            if (gameName == null) {
                continue;
            }
            if (admin.getOwner(gameName).equals(player)) {
                if (admin.isOpen(gameName)) {
                    games.put(gameName, new RegistrationSummaryBean(abean, gameName));
                } else if (!admin.isFinished(gameName)) {
                    c.add(gameName);
                }
            }
        }
        rGames = c.toArray(new String[0]);
        Arrays.sort(rGames);
        players = admin.getPlayers();
        Arrays.sort(players);
    }

    public Map<String, RegistrationSummaryBean> getGames() {
        return games;
    }

    public String[] getRunningGames() {
        return rGames;
    }

    public String[] getPlayers() {
        return players;
    }
}
