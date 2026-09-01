package net.deckserver.services;

import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.game.cards.CryptCard;
import net.deckserver.game.cards.CryptType;
import net.deckserver.game.cards.LibraryCard;
import net.deckserver.rest.bean.CardDetailBean;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Search, scoring and DTO projection over {@link CardRegistry} — the read side
 * of the deck editor's card lookups. Plain static class, matching
 * {@link CardRegistry}.
 */
public final class CardSearchService {

    private static final int SUGGESTION_LIMIT = 5;

    private CardSearchService() {
    }

    /** Up to {@value #SUGGESTION_LIMIT} best matches for a partial card name. */
    public static List<CardDetailBean> autocomplete(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalizedQuery = StringUtils.stripAccents(query).toLowerCase();

        record Match(String displayName, Card card, int score) {}
        Map<String, Match> best = new HashMap<>();

        for (Map.Entry<String, Card> entry : CardRegistry.lookupEntries().entrySet()) {
            String normalizedKey = StringUtils.stripAccents(entry.getKey()).toLowerCase();
            int score = matchScore(normalizedQuery, normalizedKey);
            if (score < 0) continue;

            String displayName = canonicalName(entry.getValue());
            Match existing = best.get(displayName);
            if (existing == null || score < existing.score()) {
                best.put(displayName, new Match(displayName, entry.getValue(), score));
            }
        }

        return best.values().stream()
                .sorted(Comparator.comparingInt(Match::score)
                        .thenComparingInt(m -> m.displayName().length())
                        .thenComparing(Match::displayName, String.CASE_INSENSITIVE_ORDER))
                .limit(SUGGESTION_LIMIT)
                .map(m -> toDetail(m.card()))
                .toList();
    }

    /** Batch detail lookup — used to enrich a deck's entries on load. */
    public static List<CardDetailBean> findDetailsByIds(List<String> ids) {
        return ids.stream()
                .map(String::trim)
                .map(CardRegistry::findById)
                .filter(Objects::nonNull)
                .map(CardSearchService::toDetail)
                .toList();
    }

    public static CardDetailBean findDetailById(String id) {
        Card card = CardRegistry.findById(id);
        return card != null ? toDetail(card) : null;
    }

    public static CardDetailBean toDetail(Card card) {
        if (card instanceof CryptCard c) {
            List<String> types = List.of(c.type() == CryptType.IMBUED ? "Imbued" : "Vampire");
            return new CardDetailBean(
                    c.id(), c.name(), true,
                    types, c.group(), c.banned(), c.advanced(), c.sets(),
                    c.clan(), c.path(), c.capacity(), c.disciplines(),
                    List.of(), List.of(), List.of(), null, null, null);
        }
        LibraryCard l = (LibraryCard) card;
        return new CardDetailBean(
                l.id(), l.name(), false,
                l.types(), null, l.banned(), false, l.sets(),
                null, null, null, List.of(),
                l.andDisciplines(), l.orDisciplines(),
                l.requirementClans(), l.requirementPath(),
                l.poolCost(), l.bloodCost());
    }

    // ── Scoring ──────────────────────────────────────────────────────────────

    /**
     * Score a normalized query against a normalized lookup key — lower is
     * better. 0 = exact, 1 = word-prefix, 2 = key-prefix, 3 = contains,
     * 4 = fuzzy word-prefix, -1 = no match.
     */
    private static int matchScore(String normalizedQuery, String normalizedKey) {
        if (normalizedKey.equals(normalizedQuery)) return 0;
        for (String token : normalizedKey.split("[\\s(]")) {
            if (!token.isEmpty() && token.startsWith(normalizedQuery)) return 1;
        }
        if (normalizedKey.startsWith(normalizedQuery)) return 2;
        if (normalizedKey.contains(normalizedQuery)) return 3;
        int fuzzyLen = Math.max(normalizedQuery.length() - 2, 4);
        if (normalizedQuery.length() >= fuzzyLen) {
            String queryPrefix = normalizedQuery.substring(0, fuzzyLen);
            for (String token : normalizedKey.split("[\\s(]")) {
                if (token.length() >= fuzzyLen && token.startsWith(queryPrefix)) return 4;
            }
        }
        return -1;
    }

    private static String canonicalName(Card card) {
        return card.displayName();
    }
}
