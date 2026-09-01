package net.deckserver.storage.json.deck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collapses every historical {@code deck_content} representation down to the one
 * canonical {@link Deck} model:
 *
 * <ul>
 *   <li>{@code ExtendedDeck} JSON — the current stored shape, {@code {"deck": …, "stats": …, "errors": …}} — is unwrapped to its {@code deck}</li>
 *   <li>bare {@link Deck} JSON / KRCG JSON ({@code {"crypt": …, "library": …}}, card ids as string or int) is read directly</li>
 *   <li>plain deck-list text and legacy {@code z@…@z} JOL exports are run through {@link DeckParser#parseDeck(String)}</li>
 * </ul>
 *
 * <p>Every card id is re-pointed at its canonical name from the current card
 * database (names in old exports drift); unknown ids are kept as-is so
 * {@link DeckParser#analyze(Deck)} can report them. Crypt/library counts are
 * recomputed. Derived {@code stats}/{@code errors} are never carried over —
 * they're recomputed on read.
 *
 * <p>This is the primitive the deck-storage migration (both the running app's
 * read path and {@code migrate-to-db.sh}) will use to land on Deck JSON as the
 * single stored form.
 */
public final class DeckNormalizer {

    private static final Logger logger = LoggerFactory.getLogger(DeckNormalizer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private DeckNormalizer() {
    }

    /** Any historical deck-content string to the canonical {@link Deck}. */
    public static Deck normalize(String content) {
        if (content == null || content.isBlank()) {
            return emptyDeck();
        }
        String trimmed = content.strip();

        if (trimmed.startsWith("{")) {
            try {
                JsonNode root = MAPPER.readTree(trimmed);
                JsonNode deckNode = root.has("deck") ? root.get("deck") : root;
                Deck deck = MAPPER.treeToValue(deckNode, Deck.class);
                if (deck == null) {
                    return emptyDeck();
                }
                if (deck.getCrypt() == null) deck.setCrypt(new Crypt());
                if (deck.getLibrary() == null) deck.setLibrary(new Library());
                reResolve(deck);
                DeckParser.analyze(deck); // recompute counts in place
                return deck;
            } catch (Exception e) {
                logger.warn("Deck content began with '{{' but did not parse as JSON; treating as text", e);
            }
        }

        return DeckParser.parseDeck(trimmed).getDeck();
    }

    /**
     * Re-points every {@link CardCount} at the canonical name for its id.
     * Unknown ids are left untouched so a later {@code analyze()} flags them.
     */
    private static void reResolve(Deck deck) {
        deck.getCrypt().getCards().forEach(DeckNormalizer::reResolveCard);
        deck.getLibrary().getCards().forEach(group -> group.getCards().forEach(DeckNormalizer::reResolveCard));
    }

    private static void reResolveCard(CardCount cardCount) {
        if (cardCount.getId() == null) {
            return;
        }
        Card card = CardRegistry.findById(String.valueOf(cardCount.getId()));
        if (card != null) {
            cardCount.setName(card.name());
        }
    }

    private static Deck emptyDeck() {
        Deck deck = new Deck();
        deck.setCrypt(new Crypt());
        deck.setLibrary(new Library());
        return deck;
    }
}
