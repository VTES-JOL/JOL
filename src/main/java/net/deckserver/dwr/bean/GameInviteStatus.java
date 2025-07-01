package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;

@Getter
public class GameInviteStatus {
    private final String gameName;
    private final String playerName;
    private final String deckName;
    private final String format;

    public GameInviteStatus(String gameName, String playerName) {
        this.gameName = gameName;
        this.playerName = playerName;
        JolAdmin admin = JolAdmin.INSTANCE;
        this.format = admin.getFormat(gameName);
        this.deckName = admin.isRegistered(gameName, playerName) ? admin.getRegistration(gameName, playerName).getDeckName() : null;
    }
}
