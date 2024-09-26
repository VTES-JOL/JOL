package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Getter
public class GameSummaryBean {

    private final String gameName;
    private final String turn;
    private final String owner;

    public GameSummaryBean(String gameName) {
        JolAdmin admin = JolAdmin.getInstance();
        this.gameName = gameName;
        this.turn = admin.getGame(gameName).getCurrentTurn();
        this.owner = admin.getOwner(gameName);
    }

}
