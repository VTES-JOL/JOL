package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.storage.cards.Deck;

public class PlayerRegistrationBean {

    private final String player;
    private final boolean registered;
    private final String deckSummary;
    private final boolean valid;

    public PlayerRegistrationBean(String player, String game) {
        this.player = player;
        String deck = JolAdmin.getInstance().getGameDeck(game, player);
        this.registered = !deck.equals(JolAdmin.DECK_NOT_FOUND);
        if (this.registered) {
            Deck impl = new Deck(CardSearch.INSTANCE, deck);
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
