package deckserver.dwr.bean;

import deckserver.dwr.GameModel;
import deckserver.dwr.GameView;
import deckserver.JolAdminFactory;

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

    public SummaryBean(GameModel game) {
        this.game = game.getName();
        if (JolAdminFactory.INSTANCE.isActive(this.game)) {
            access = getDate(game.getTimestamp());
            turn = JolAdminFactory.INSTANCE.getGame(this.game).getCurrentTurn();
            GameView[] views = game.getViews();
            Collection<String> actives = new ArrayList<String>(5);
            for (int i = 0; i < views.length; i++) {
                if (views[i].isPlayer()) actives.add(views[i].getPlayer());
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

}
