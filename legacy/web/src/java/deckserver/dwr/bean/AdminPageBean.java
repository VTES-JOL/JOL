package deckserver.dwr.bean;

import java.util.*;

import nbclient.vtesmodel.JolAdminFactory;
import deckserver.rich.*;

public class AdminPageBean {

    // map of Game name -> RegistrationSummaryBean[]
    private Map<String, RegistrationSummaryBean> games = new HashMap<String, RegistrationSummaryBean>();
    private String[] players;
    private String[] rGames;

    public AdminPageBean(AdminBean abean, String player) {
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        String[] names = admin.getGames(player);
        Collection<String> c = new ArrayList<String>();
        for (int i = 0; i < names.length; i++) {
            if (admin.getOwner(names[i]).equals(player)) {
                if (admin.isOpen(names[i])) {
                    games.put(names[i], new RegistrationSummaryBean(abean, names[i]));
                } else if (!admin.isFinished(names[i])){
                    c.add(names[i]);
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
