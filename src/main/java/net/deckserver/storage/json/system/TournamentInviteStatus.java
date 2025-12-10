package net.deckserver.storage.json.system;

import lombok.Data;

@Data
public class TournamentInviteStatus {

    private final String name;
    private final String deck;
    private final String format;

    public TournamentInviteStatus(TournamentDefinition definition, String player) {
        name = definition.getName();
        deck = definition.getRegistration(player).map(TournamentRegistration::getDeck).orElse(null);
        format = definition.getDeckFormat().toString();
    }
}
