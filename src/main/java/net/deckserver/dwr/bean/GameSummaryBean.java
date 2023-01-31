package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class GameSummaryBean {

    private final String gameName;
    private final String access;
    private final String turn;
    private final String owner;

    public GameSummaryBean(String gameName) {
        JolAdmin admin = JolAdmin.getInstance();
        this.gameName = gameName;
        this.access = admin.getGameTimeStamp(gameName).format(ISO_OFFSET_DATE_TIME);
        this.turn = admin.getGame(gameName).getCurrentTurn();
        this.owner = admin.getOwner(gameName);
    }

    public String getGameName() {
        return gameName;
    }

    public String getAccess() {
        return access;
    }

    public String getTurn() {
        return turn;
    }

    public String getOwner() {
        return owner;
    }
}
