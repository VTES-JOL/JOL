package net.deckserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.rest.bean.ImportPreviewBean;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.DeckParser;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.deck.LibraryCard;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deck-import parsing for the editor: turns a pasted KRCG JSON document or a
 * plain JOL deck list into a preview of resolved cards + unresolved lines, and
 * builds canonical deck-list text from confirmed {cardId, count} entries.
 *
 * <p>Plain static class, matching {@link net.deckserver.game.cards.CardRegistry} / {@link CardSearchService}.
 * The JOL-text branch delegates to the existing {@link DeckParser}; the KRCG
 * branch resolves ids straight against {@link CardRegistry}.
 */
public final class DeckImportService {

    private static final Logger logger = LoggerFactory.getLogger(DeckImportService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DeckImportService() {
    }

    /** Auto-detects the format and returns what would be imported. */
    public static ImportPreviewBean preview(String text) {
        String trimmed = text == null ? "" : text.strip();
        return trimmed.startsWith("{") ? previewKrcg(trimmed) : previewJol(trimmed);
    }

    /** Canonical `"{count} x {name}"` deck-list text from confirmed entries. */
    public static String buildContents(List<Entry> entries) {
        StringBuilder crypt = new StringBuilder();
        StringBuilder library = new StringBuilder();
        for (Entry entry : entries) {
            Card card = CardRegistry.findById(entry.cardId());
            if (card == null) continue;
            StringBuilder target = card.isCrypt() ? crypt : library;
            target.append(entry.count()).append(" x ").append(card.name()).append('\n');
        }
        return crypt + "\n" + library;
    }

    // ── JOL text ─────────────────────────────────────────────────────────────

    private static ImportPreviewBean previewJol(String text) {
        ExtendedDeck parsed = DeckParser.parseDeck(text);
        List<ImportPreviewBean.ResolvedEntry> resolved = new ArrayList<>();

        parsed.getDeck().getCrypt().getCards().forEach(cc -> addResolved(resolved, cc));
        for (LibraryCard group : parsed.getDeck().getLibrary().getCards()) {
            group.getCards().forEach(cc -> addResolved(resolved, cc));
        }

        List<ImportPreviewBean.ParseError> errors = parsed.getErrors().stream()
                .map(line -> new ImportPreviewBean.ParseError(line, "Card not found"))
                .toList();

        return new ImportPreviewBean("jol", null, null, resolved, errors);
    }

    private static void addResolved(List<ImportPreviewBean.ResolvedEntry> out, CardCount cc) {
        Card card = cc.getId() == null ? null : CardRegistry.findById(String.valueOf(cc.getId()));
        if (card != null) {
            out.add(new ImportPreviewBean.ResolvedEntry(cc.getCount(), CardSearchService.toDetail(card)));
        }
    }

    // ── KRCG JSON ────────────────────────────────────────────────────────────

    private static ImportPreviewBean previewKrcg(String text) {
        List<ImportPreviewBean.ResolvedEntry> resolved = new ArrayList<>();
        List<ImportPreviewBean.ParseError> errors = new ArrayList<>();
        String deckName = null;
        String deckDescription = null;

        try {
            JsonNode root = MAPPER.readTree(text);

            deckName = firstNonBlank(root.path("name").asText(null), root.path("meta").path("name").asText(null));
            String rawComments = firstNonBlank(root.path("comments").asText(null),
                    root.path("meta").path("description").asText(null));
            if (rawComments != null) {
                deckDescription = rawComments.strip();
            }

            for (JsonNode card : root.path("crypt").path("cards")) {
                resolveKrcgCard(card, resolved, errors);
            }
            for (JsonNode group : root.path("library").path("cards")) {
                for (JsonNode card : group.path("cards")) {
                    resolveKrcgCard(card, resolved, errors);
                }
            }
        } catch (Exception e) {
            logger.warn("KRCG import preview failed to parse", e);
            errors.add(new ImportPreviewBean.ParseError(
                    text.length() > 60 ? text.substring(0, 60) + "…" : text,
                    "Invalid JSON: " + e.getMessage()));
        }

        return new ImportPreviewBean("krcg", deckName, deckDescription, resolved, errors);
    }

    private static void resolveKrcgCard(JsonNode card, List<ImportPreviewBean.ResolvedEntry> resolved,
                                        List<ImportPreviewBean.ParseError> errors) {
        JsonNode idNode = card.path("id");
        String id = idNode.isMissingNode() || idNode.isNull() ? null : idNode.asText();
        int count = card.path("count").asInt(1);
        if (id == null || id.isBlank()) {
            errors.add(new ImportPreviewBean.ParseError(card.toString(), "Missing card id"));
            return;
        }
        Card found = CardRegistry.findById(id);
        if (found == null) {
            errors.add(new ImportPreviewBean.ParseError(id, "Unknown card id: " + id));
            return;
        }
        resolved.add(new ImportPreviewBean.ResolvedEntry(count, CardSearchService.toDetail(found)));
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.isNotBlank(a)) return a;
        if (StringUtils.isNotBlank(b)) return b;
        return null;
    }

    /** A confirmed import entry from the client. */
    public record Entry(String cardId, int count) {
    }

    /** Convenience for {@link #buildContents(List)} callers that hold a map. */
    public static String buildContents(Map<String, Integer> cardCounts) {
        return buildContents(cardCounts.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList());
    }
}
