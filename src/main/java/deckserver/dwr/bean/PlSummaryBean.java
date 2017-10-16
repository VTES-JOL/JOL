package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.GameView;
import deckserver.game.cards.Deck;
import deckserver.game.cards.DeckImpl;

import java.util.Date;

public class PlSummaryBean {

    private int library = 0;

    private int crypt = 0;

    private String groups = "";

    private boolean started;

    private boolean current = true;

    private String game;

    private String turn;

    public PlSummaryBean(GameModel game, String player) {
        this.game = game.getName();
        this.started = game.isActive();
        JolAdmin admin = JolAdmin.getInstance();
        if (!started) {
            String deck = admin.getGameDeck(game.getName(), player);
            Deck nd = new DeckImpl(admin.getAllCards(), deck);
            library = nd.getLibSize();
            crypt = nd.getCryptSize();
            groups = nd.getGroups();
        } else {
            turn = JolAdmin.getInstance().getGame(this.game).getActivePlayer();
            GameView view = game.hasView(player);
            if (view != null) {
                this.current = !view.isChanged();
            } else {
                Date access = admin.getAccess(this.game, player);
                Date timestamp = admin.getGameTimeStamp(this.game);
                Date systemStart = admin.getSystemStart();
                if (timestamp.compareTo(systemStart) < 0) {
                    timestamp = systemStart;
                }
                this.current = timestamp.before(access);
            }
        }
    }

    public String getGame() {
        return game;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isStarted() {
        return started;
    }

    public int getCryptSize() {
        return crypt;
    }

    public int getLibSize() {
        return library;
    }

    public String getGroups() {
        return groups;
    }

    public String getTurn() {
        return turn;
    }
}
