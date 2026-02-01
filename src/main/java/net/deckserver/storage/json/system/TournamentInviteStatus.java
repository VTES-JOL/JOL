package net.deckserver.storage.json.system;

import lombok.Data;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.ExtendedDeck;

@Data
public class TournamentInviteStatus {

    private final String name;
    private final Deck deck;
    private final String format;

    public TournamentInviteStatus(TournamentDefinition definition, String player) {
        name = definition.getName();
        deck = definition.getRegistration(player).map(TournamentRegistration::getDeck)
                .map(deckId -> TournamentService.getTournamentDeck(name, deckId))
                .map(ExtendedDeck::getDeck)
                .orElse(null);
        format = definition.getDeckFormat().toString();
    }
}
