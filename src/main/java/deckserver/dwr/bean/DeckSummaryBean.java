package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.PlayerModel;
import deckserver.game.cards.NormalizeDeck;
import deckserver.game.cards.NormalizeDeckFactory;
import deckserver.game.cards.OldCardSearch;

public class DeckSummaryBean {

    public String name, game;
    public int lib, crypt;
    public String groups;

    // get information about a player's deck
    public DeckSummaryBean(PlayerModel model, String name) {
        this.name = name;
        game = null;
        init(model.getPlayer(), JolAdmin.INSTANCE.getDeck(model.getPlayer(), name));
    }

    // get information about the player's deck that is registered for a game
    public DeckSummaryBean(GameModel game, PlayerModel model) {
        this.game = game.getName();
        name = JolAdmin.INSTANCE.getDeckName(this.game, model.getPlayer());
        init(model.getPlayer(), JolAdmin.INSTANCE.getGameDeck(this.game, model.getPlayer()));
    }

    private void init(String player, String deck) {
        OldCardSearch search = JolAdmin.INSTANCE.getAllCards();
        try {
            NormalizeDeck nd = NormalizeDeckFactory.getDeckSize(search, deck);
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
