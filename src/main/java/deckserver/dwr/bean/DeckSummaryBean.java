package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.PlayerModel;
import deckserver.game.cards.CardSearch;
import deckserver.game.cards.Deck;
import deckserver.game.cards.DeckImpl;

public class DeckSummaryBean {

    public String name, game;
    public int lib, crypt;
    public String groups;

    public DeckSummaryBean(PlayerModel model, String name, boolean read) {
        this.name = name;
        game = null;
        if (read) {
            init(model.getPlayer(), JolAdmin.getInstance().getDeck(model.getPlayer(), name));
        }
    }

    // get information about a player's deck
    public DeckSummaryBean(PlayerModel model, String name) {
        this(model, name, true);
    }

    // get information about the player's deck that is registered for a game
    public DeckSummaryBean(GameModel game, PlayerModel model) {
        this.game = game.getName();
        name = JolAdmin.getInstance().getDeckName(this.game, model.getPlayer());
        init(model.getPlayer(), JolAdmin.getInstance().getGameDeck(this.game, model.getPlayer()));
    }

    private void init(String player, String deck) {
        CardSearch search = JolAdmin.getInstance().getAllCards();
        try {
            Deck nd = new DeckImpl(search, deck);
            lib = nd.getLibSize();
            crypt = nd.getCryptSize();
            groups = nd.getGroups();
        } catch (Throwable t) {
            lib = 0;
            crypt = 0;
            groups = "ERROR";
            String msg = "Error in deck " + name + " for player " + player;
        }
    }

    public int getCrypt() {
        return crypt;
    }

    public String getGame() {
        return game;
    }

    public String getGroups() {
        return groups;
    }

    public int getLib() {
        return lib;
    }

    public String getName() {
        return name;
    }

}
