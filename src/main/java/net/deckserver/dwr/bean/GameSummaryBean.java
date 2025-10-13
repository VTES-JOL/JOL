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
        this.gameName = gameName;
        this.turn = JolAdmin.getGame(gameName).getTurnLabel();
        this.timestamp = JolAdmin.getGameTimeStamp(gameName).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
