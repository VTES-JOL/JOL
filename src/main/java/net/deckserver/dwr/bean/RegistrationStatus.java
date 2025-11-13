package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.services.RegistrationService;

@Getter
public class RegistrationStatus {
    private final String player;
    private final String gameName;
    private final boolean registered;
    private final String deckSummary;
    private final boolean valid;

    public RegistrationStatus(String game, String player) {
        this.player = player;
        this.gameName = game;
        net.deckserver.storage.json.system.RegistrationStatus status = RegistrationService.getRegistration(game, player);
        this.registered = status.getDeckId() != null;
        this.deckSummary = status.getSummary();
        this.valid = status.isValid();
    }

}
