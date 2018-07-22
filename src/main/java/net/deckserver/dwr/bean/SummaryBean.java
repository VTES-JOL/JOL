package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;

import java.util.ArrayList;
import java.util.Collection;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class SummaryBean {

    private String game;
    private String access = "none";
    private String turn = null;
    private String[] available = new String[0];
    private String admin;

    public SummaryBean(GameModel game) {
        this.game = game.getName();
        if (JolAdmin.getInstance().isActive(this.game)) {
            access = JolAdmin.getInstance().getGameTimeStamp(this.game).format(ISO_OFFSET_DATE_TIME);
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
