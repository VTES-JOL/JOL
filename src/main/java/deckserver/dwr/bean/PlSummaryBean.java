package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.client.JolGame;
import deckserver.dwr.GameModel;
import deckserver.dwr.GameView;
import deckserver.game.cards.Deck;
import deckserver.game.cards.DeckImpl;

import java.util.Date;

public class PlSummaryBean {

    private int libSize = 0;

    private int cryptSize = 0;

    private String groups = "";

    private boolean started;

    private boolean current = true;

    private String game;

    private String turn;

    private boolean hidden = true;

    public PlSummaryBean(GameModel game, String player) {
        this.game = game.getName();
        this.started = game.isActive();
        JolAdmin admin = JolAdmin.getInstance();
        if (!started) {
            String deck = admin.getGameDeck(game.getName(), player);
            Deck nd = new DeckImpl(admin.getAllCards(), deck);
            libSize = nd.getLibSize();
            cryptSize = nd.getCryptSize();
            groups = nd.getGroups();
        } else {
            JolGame thisGame = JolAdmin.getInstance().getGame(this.game);
            turn = thisGame.getActivePlayer();
            hidden = thisGame.getPool(player) <= 0;
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
        return cryptSize;
    }

    public int getLibSize() {
        return libSize;
    }

    public String getGroups() {
        return groups;
    }

    public String getTurn() {
        return turn;
    }

    public boolean isHidden() {
        return hidden;
    }
}
