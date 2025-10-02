package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;

import java.time.format.DateTimeFormatter;

@Getter
public class GameSummaryBean {

    private final String gameName;
    private final String turn;
    private final String timestamp;

    public GameSummaryBean(String gameName) {
        JolAdmin admin = JolAdmin.INSTANCE;
        this.gameName = gameName;
        this.turn = admin.getGame(gameName).getCurrentTurn();
        this.timestamp = admin.getGameTimeStamp(gameName).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
