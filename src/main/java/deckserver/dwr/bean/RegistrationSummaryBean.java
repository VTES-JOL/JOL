package deckserver.dwr.bean;

import deckserver.client.JolAdminFactory;
import deckserver.dwr.GameModel;

import java.util.HashMap;
import java.util.Map;

public class RegistrationSummaryBean {

    Map<String, PlSummaryBean> registrations = new HashMap<>();

    public RegistrationSummaryBean(AdminBean abean, String name) {
        GameModel game = abean.getGameModel(name);
        JolAdminFactory admin = abean.getAdmin();
        String[] players = admin.getPlayers();
        for (String player : players) {
            if (admin.isInvited(name, player) || admin.getGameDeck(name, player) != null) {
                registrations.put(player, new PlSummaryBean(game, player));
            }
        }
    }

    public Map<String, PlSummaryBean> getRegistrations() {
        return registrations;
    }
}
