package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.services.RegistrationService;

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
        this.deckName = RegistrationService.isRegistered(gameName, playerName) ? RegistrationService.getRegistration(gameName, playerName).getDeckName() : null;
    }
}
