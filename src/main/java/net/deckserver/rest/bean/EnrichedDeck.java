package net.deckserver.rest.bean;

import net.deckserver.storage.json.deck.Deck;

import java.util.Map;

/**
 * A deck plus per-card display data (clan / discipline / path / cost / banned
 * / …), keyed by card id rendered as a string.
 *
 * <p>This is the single wire shape behind every read-only deck view — the deck
 * editor, the lobby registration preview, the in-game deck panel and the
 * tournament registration preview — so one React component ({@code DeckView})
 * can render all of them with icons instead of a follow-up per-card fetch.
 * Built by {@link net.deckserver.services.DeckEnrichmentService#enrich(Deck)}.
 */
public record EnrichedDeck(Deck deck, Map<String, CardDetailBean> details) {
}
