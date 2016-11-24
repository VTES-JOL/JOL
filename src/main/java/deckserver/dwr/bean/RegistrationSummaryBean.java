package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;

import java.util.HashMap;
import java.util.Map;

public class RegistrationSummaryBean {

    Map<String, PlSummaryBean> registrations = new HashMap<>();

    public RegistrationSummaryBean(AdminBean abean, String name) {
        GameModel game = abean.getGameModel(name);
        JolAdmin admin = JolAdmin.getInstance();
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
