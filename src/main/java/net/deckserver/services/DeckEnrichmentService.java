package net.deckserver.services;

import net.deckserver.rest.bean.CardDetailBean;
import net.deckserver.rest.bean.EnrichedDeck;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.LibraryCard;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Projects a {@link Deck} into an {@link EnrichedDeck} — the deck tree plus a
 * {@code card-id → }{@link CardDetailBean} map covering every distinct card it
 * contains (clan, disciplines, path, capacity, requirement icons, costs, …).
 *
 * <p>Pure in-memory work over
 * {@link net.deckserver.game.cards.CardRegistry} (via
 * {@link CardSearchService#toDetail}); no database access, so it is cheap
 * enough to run on every deck read and unit-testable without a container.
 */
public final class DeckEnrichmentService {

    private DeckEnrichmentService() {
    }

    /** {@code deck} wrapped with per-card display detail; a null deck yields an empty map. */
    public static EnrichedDeck enrich(Deck deck) {
        return new EnrichedDeck(deck, details(deck));
    }

    /** The {@code card-id → detail} map for a deck, keyed by card id as a string. */
    public static Map<String, CardDetailBean> details(Deck deck) {
        Map<String, CardDetailBean> details = new LinkedHashMap<>();
        if (deck == null) {
            return details;
        }
        deck.getCrypt().getCards().forEach(card -> addDetail(details, card));
        deck.getLibrary().getCards().stream()
                .map(LibraryCard::getCards)
                .flatMap(java.util.List::stream)
                .forEach(card -> addDetail(details, card));
        return details;
    }

    private static void addDetail(Map<String, CardDetailBean> target, CardCount card) {
        if (card == null || card.getId() == null) {
            return;
        }
        String id = String.valueOf(card.getId());
        if (target.containsKey(id)) {
            return;
        }
        CardDetailBean detail = CardSearchService.findDetailById(id);
        if (detail != null) {
            target.put(id, detail);
        }
    }
}
