package net.deckserver.storage.json.system;

import lombok.Data;
import net.deckserver.rest.bean.CardDetailBean;
import net.deckserver.services.DeckEnrichmentService;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.ExtendedDeck;

import java.util.Map;

@Data
public class TournamentInviteStatus {

    private final String name;
    private final Deck deck;
    /** Per-card display detail for {@link #deck}, keyed by card id — lets the shared deck view render icons. */
    private final Map<String, CardDetailBean> details;
    private final String format;

    public TournamentInviteStatus(TournamentDefinition definition, String player) {
        name = definition.getName();
        deck = definition.getRegistration(player).map(TournamentRegistration::getDeck)
                .map(deckId -> TournamentService.getTournamentDeck(name, deckId))
                .map(ExtendedDeck::getDeck)
                .orElse(null);
        details = DeckEnrichmentService.details(deck);
        format = definition.getDeckFormat().toString();
    }
}
