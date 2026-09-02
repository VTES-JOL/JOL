package net.deckserver.storage.json.deck;

import net.deckserver.game.cards.CardRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Exercises {@link DeckNormalizer} against every stored-deck representation and
 * {@link DeckParser#analyze(Deck)}'s recompute-on-read behaviour. These are the
 * primitives the deck-storage migration to canonical Deck JSON is built on.
 */
class DeckNormalizerTest {

    private static final Path FIXTURE_DECKS = Path.of("src/test/resources/data/decks");

    private static String cardId(String name) {
        return CardRegistry.resolveFuzzy(name).orElseThrow(() -> new AssertionError("fixture card not found: " + name)).id();
    }

    // ── Fixture decks (all stored as ExtendedDeck JSON) ──────────────────────

    @Test
    void everyFixtureDeckNormalisesWithoutLosingCards() throws IOException {
        for (Path file : fixtureDecks()) {
            String raw = Files.readString(file);
            int sourceEntries = countCardEntries(raw);

            Deck deck = DeckNormalizer.normalize(raw);
            ExtendedDeck analyzed = DeckParser.analyze(deck);

            int normalisedEntries = deck.getCrypt().getCards().size()
                    + deck.getLibrary().getCards().stream().mapToInt(g -> g.getCards().size()).sum();

            assertThat(file + " must keep every card entry (incl. unresolved)", normalisedEntries, equalTo(sourceEntries));
            assertThat(file + " crypt", deck.getCrypt().getCards(), is(not(empty())));
            assertThat(file + " library", deck.getLibrary().getCards(), is(not(empty())));
            assertThat(file + " crypt count recomputed", deck.getCrypt().getCount(), greaterThan(0));
            assertThat(file + " library count recomputed", deck.getLibrary().getCount(), greaterThan(0));
            assertThat(file + " stats summary", analyzed.getStats().getSummary(), containsString("Crypt:"));
        }
    }

    @Test
    void everyFixtureDeckResolvesAgainstTheProductionCardDatabase() throws IOException {
        List<String> withUnknowns = new java.util.ArrayList<>();

        for (Path file : fixtureDecks()) {
            ExtendedDeck analyzed = DeckParser.analyze(DeckNormalizer.normalize(Files.readString(file)));
            if (!analyzed.getErrors().isEmpty()) {
                withUnknowns.add(file.getFileName() + " -> " + analyzed.getErrors());
            }
        }

        assertThat("fixture decks with unresolved cards: " + withUnknowns, withUnknowns, is(empty()));
    }

    private static List<Path> fixtureDecks() throws IOException {
        try (Stream<Path> files = Files.list(FIXTURE_DECKS)) {
            List<Path> decks = files.filter(p -> p.toString().endsWith(".json")).sorted().toList();
            assertThat("expected fixture decks on disk", decks, is(not(empty())));
            return decks;
        }
    }

    /** Counts CardCount objects in a stored ExtendedDeck/Deck JSON string. */
    private static int countCardEntries(String json) throws IOException {
        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        com.fasterxml.jackson.databind.JsonNode deck = root.has("deck") ? root.get("deck") : root;
        int n = deck.path("crypt").path("cards").size();
        for (com.fasterxml.jackson.databind.JsonNode group : deck.path("library").path("cards")) {
            n += group.path("cards").size();
        }
        return n;
    }

    // ── Bare Deck JSON (no ExtendedDeck wrapper) ─────────────────────────────

    @Test
    void readsBareDeckJson() {
        String json = """
                {"name":"Bare","crypt":{"count":0,"cards":[
                   {"id":%s,"name":"stale name","count":4}
                ]},"library":{"count":0,"cards":[
                   {"type":"Action","count":0,"cards":[{"id":%s,"name":"stale","count":6}]}
                ]}}
                """.formatted(cardId("Vardar Vardarian"), cardId("Govern the Unaligned"));

        Deck deck = DeckNormalizer.normalize(json);

        assertThat(deck.getName(), equalTo("Bare"));
        assertThat(deck.getCrypt().getCards().get(0).getName(), equalTo("Vardar Vardarian")); // re-resolved
        assertThat(deck.getCrypt().getCount(), equalTo(4));   // recomputed from 0
        assertThat(deck.getLibrary().getCount(), equalTo(6));
    }

    // ── KRCG JSON: string ids, top-level name/comments ──────────────────────

    @Test
    void readsKrcgJsonWithStringIds() {
        String json = """
                {"name":"KRCG","comments":"a description","crypt":{"count":1,"cards":[
                   {"id":"%s","count":3,"name":"Vardar Vardarian"}
                ]},"library":{"count":1,"cards":[
                   {"type":"Action","count":1,"cards":[{"id":"%s","count":8,"name":"Govern the Unaligned"}]}
                ]}}
                """.formatted(cardId("Vardar Vardarian"), cardId("Govern the Unaligned"));

        Deck deck = DeckNormalizer.normalize(json);
        ExtendedDeck analyzed = DeckParser.analyze(deck);

        assertThat(deck.getName(), equalTo("KRCG"));
        assertThat(deck.getComments(), equalTo("a description"));
        assertThat(deck.getCrypt().getCards().get(0).getId(), equalTo(Integer.valueOf(cardId("Vardar Vardarian"))));
        assertThat(deck.getCrypt().getCount(), equalTo(3));
        assertThat(deck.getLibrary().getCount(), equalTo(8));
        assertThat(analyzed.getErrors(), is(empty()));
    }

    // ── Plain text ──────────────────────────────────────────────────────────

    @Test
    void parsesPlainText() {
        Deck deck = DeckNormalizer.normalize("""
                Crypt (2)
                2 Vardar Vardarian

                Library (3)
                3 Govern the Unaligned
                """);

        assertThat(deck.getCrypt().getCards(), hasSize(1));
        assertThat(deck.getCrypt().getCards().get(0).getName(), equalTo("Vardar Vardarian"));
        assertThat(deck.getLibrary().getCards().get(0).getCards().get(0).getName(), equalTo("Govern the Unaligned"));
    }

    // ── analyze() recompute + error reporting ──────────────────────────────

    @Test
    void analyzeRecomputesCountsAndFlagsUnknownIds() {
        Deck deck = new Deck();
        Crypt crypt = new Crypt();
        crypt.setCount(999); // deliberately wrong
        crypt.getCards().add(new CardCount(Integer.valueOf(cardId("Vardar Vardarian")), "Vardar Vardarian", 4, null));
        crypt.getCards().add(new CardCount(1, "Ghost Card", 2, null)); // id 1 never resolves
        deck.setCrypt(crypt);
        deck.setLibrary(new Library());

        ExtendedDeck analyzed = DeckParser.analyze(deck);

        assertThat(deck.getCrypt().getCount(), equalTo(6));
        assertThat(analyzed.getErrors(), contains(containsString("Ghost Card")));
    }

    /**
     * {@link DeckNormalizer#normalize} on plain text goes through
     * {@link DeckParser#parseDeck} and keeps only the resolved cards — an
     * unrecognised line is dropped, not surfaced. The storage-migration
     * "does this LEGACY deck parse cleanly?" check must therefore use
     * {@code parseDeck} directly (which reports it as an error), not
     * {@code analyze(normalize(...))}.
     */
    @Test
    void normalizePlainTextDropsUnresolvedLinesButParseDeckReportsThem() {
        String text = "2 Vardar Vardarian\n1 feeding razor\n";

        Deck normalised = DeckNormalizer.normalize(text);
        assertThat("unresolved line is not carried into the Deck",
                DeckParser.analyze(normalised).getErrors(), is(empty()));

        assertThat("parseDeck keeps it as an error",
                DeckParser.parseDeck(text).getErrors(), contains(containsString("feeding razor")));
    }

    @Test
    void handlesNullAndBlankContent() {
        assertThat(DeckNormalizer.normalize(null).getCrypt().getCards(), is(empty()));
        assertThat(DeckNormalizer.normalize("   ").getLibrary().getCards(), is(empty()));
    }
}
