package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.storage.json.system.RegistrationStatus;

public class PlayerRegistrationStatusBean {
    private final String player;
    private final String gameName;
    private final boolean registered;
    private final String deckSummary;
    private final boolean valid;

    public PlayerRegistrationStatusBean(String game, String player) {
        this.player = player;
        this.gameName = game;
        JolAdmin admin = JolAdmin.getInstance();
        RegistrationStatus status = admin.getRegistration(game, player);
        this.registered = status.getDeckId() != null;
        this.deckSummary = status.getSummary();
        this.valid = status.isValid();
    }

    public String getPlayer() {
        return player;
    }

    public String getGameName() {
        return gameName;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getDeckSummary() {
        return deckSummary;
    }

    public boolean isValid() {
        return valid;
    }
}
