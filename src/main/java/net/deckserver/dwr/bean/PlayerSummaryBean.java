package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.storage.cards.Deck;

import java.util.Date;

public class PlayerSummaryBean {

    private int libSize = 0;

    private int cryptSize = 0;

    private String groups = "";

    private boolean started;

    private boolean current = true;

    private String game;

    private String turn;

    private boolean hidden = false;

    private boolean flagged = false;

    public PlayerSummaryBean(GameModel game, String player) {
        this.game = game.getName();
        this.started = game.isActive();
        JolAdmin admin = JolAdmin.getInstance();
        if (!started) {
            String deck = admin.getGameDeck(game.getName(), player);
            Deck nd = new Deck(admin.getAllCards(), deck);
            libSize = nd.getLibSize();
            cryptSize = nd.getCryptSize();
            groups = nd.getGroups();
        } else {
            JolGame thisGame = JolAdmin.getInstance().getGame(this.game);
            turn = thisGame.getActivePlayer();
            hidden = thisGame.getPool(player) == 0;
            flagged = thisGame.getPool(player) < 0;
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

    public boolean isFlagged() {
        return flagged;
    }
}
