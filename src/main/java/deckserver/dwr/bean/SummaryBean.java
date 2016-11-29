package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.GameView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SummaryBean {

    private static DateFormat format = new SimpleDateFormat("HH:mm M/d ");
    private String game;
    private String access = "none";
    private String turn = null;
    private String[] available = new String[0];
    private String admin;

    public SummaryBean(GameModel game) {
        this.game = game.getName();
        if (JolAdmin.getInstance().isActive(this.game)) {
            access = getDate(game.getTimestamp());
            turn = JolAdmin.getInstance().getGame(this.game).getCurrentTurn();
            admin = JolAdmin.getInstance().getOwner(this.game);
            GameView[] views = game.getViews();
            Collection<String> actives = new ArrayList<>(5);
            for (GameView view : views) {
                if (view.isPlayer()) actives.add(view.getPlayer());
            }
            available = actives.toArray(new String[0]);
        }
    }

    private String getDate(long timestamp) {
        return format.format(new Date(timestamp));
    }

    public String getAccess() {
        return access;
    }

    public String[] getAvailable() {
        return available;
    }

    public String getGame() {
        return game;
    }

    public String getTurn() {
        return turn;
    }

    public String getAdmin() {
        return admin;
    }
}
