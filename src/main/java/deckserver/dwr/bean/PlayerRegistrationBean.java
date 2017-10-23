package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.game.cards.Deck;

public class PlayerRegistrationBean {

    private final String player;
    private final boolean registered;
    private final String deckSummary;
    private final boolean valid;

    public PlayerRegistrationBean(AdminBean abean, String player, String game) {
        this.player = player;
        String deck = JolAdmin.getInstance().getGameDeck(game, player);
        this.registered = deck != null;
        if (this.registered) {
            Deck impl = new Deck(JolAdmin.getInstance().getAllCards(), deck, true);
            this.deckSummary = "Crypt: " + impl.getCryptSize() + ", Library: " + impl.getLibSize() + ", Groups: " + impl.getGroups();
            this.valid = impl.isValid();
        } else {
            this.deckSummary = "invited";
            this.valid = false;
        }
    }

    public String getPlayer() {
        return player;
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
