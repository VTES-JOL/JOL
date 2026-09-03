package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RegistrationService;

@Getter
public class RegistrationStatus {
    private final String player;
    /** Stable, URL-safe id for {@link #player} — the path token for that player's deck endpoints. */
    private final String playerId;
    private final String gameName;
    private final boolean registered;
    private final String deckName;
    private final String deckSummary;
    private final boolean valid;

    public RegistrationStatus(String game, String player) {
        this.player = player;
        this.playerId = PlayerService.getPlayerId(player);
        this.gameName = game;
        net.deckserver.storage.json.system.RegistrationStatus status = RegistrationService.getRegistration(game, player);
        this.registered = status != null && status.getDeckId() != null;
        this.deckName = status == null ? null : status.getDeckName();
        this.deckSummary = status == null ? null : status.getSummary();
        this.valid = status != null && status.isValid();
    }

}
