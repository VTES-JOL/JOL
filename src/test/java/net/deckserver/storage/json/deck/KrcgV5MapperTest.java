package net.deckserver.storage.json.deck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.cards.CardRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link KrcgV5Mapper} round-trips the canonical {@link Deck} model through the
 * KRCG v5 document that is now the stored form, and {@link DeckNormalizer}
 * reads that form back.
 */
class KrcgV5MapperTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Advanced Alan Sovereign — its own VEKN id, group 3. */
    private static final int ALAN_SOVEREIGN_ADV = 200041;

    private static int cardId(String name) {
        return Integer.parseInt(CardRegistry.resolveFuzzy(name)
                .orElseThrow(() -> new AssertionError("card not found: " + name)).id());
    }

    private static Deck sampleDeck() {
        Deck deck = new Deck();
        deck.setName("My Deck");
        deck.setComments("notes here");
        deck.getCrypt().getCards().add(new CardCount(ALAN_SOVEREIGN_ADV, "Alan Sovereign", 2, null));
        deck.getCrypt().getCards().add(new CardCount(cardId("Nkechi"), "Nkechi", 4, null));

        LibraryCard actions = new LibraryCard();
        actions.setType("Action");
        actions.getCards().add(new CardCount(cardId("Govern the Unaligned"), "Govern the Unaligned", 10, null));
        LibraryCard master = new LibraryCard();
        master.setType("Master");
        master.getCards().add(new CardCount(cardId("Villein"), "Villein", 6, "ramp"));
        LibraryCard reaction = new LibraryCard();
        reaction.setType("Reaction");
        reaction.getCards().add(new CardCount(cardId("Deflection"), "Deflection", 2, null));
        deck.getLibrary().getCards().addAll(List.of(actions, master, reaction));
        return deck;
    }

    @Test
    void roundTripsEveryCardAndCount() {
        Deck original = sampleDeck();

        Deck back = KrcgV5Mapper.fromJson(KrcgV5Mapper.toJson(original));
        ExtendedDeck analyzed = DeckParser.analyze(back);

        assertThat(analyzed.getErrors(), is(empty()));
        assertThat(back.getCrypt().getCount(), equalTo(6));
        assertThat(back.getLibrary().getCount(), equalTo(18));

        assertThat(back.getCrypt().getCards().stream().map(CardCount::getName).toList(),
                containsInAnyOrder("Alan Sovereign", "Nkechi"));
        assertThat(back.getLibrary().getCards().stream()
                        .flatMap(g -> g.getCards().stream()).map(CardCount::getName).toList(),
                containsInAnyOrder("Govern the Unaligned", "Villein", "Deflection"));

        // per-card comment survives
        assertThat(back.getLibrary().getCards().stream()
                        .flatMap(g -> g.getCards().stream())
                        .filter(c -> c.getName().equals("Villein")).findFirst().orElseThrow().getComments(),
                equalTo("ramp"));
    }

    @Test
    void writesFlatV5ShapeWithCryptSuffixes() throws Exception {
        JsonNode root = JSON.readTree(KrcgV5Mapper.toJson(sampleDeck()));

        assertThat(root.has("crypt"), is(false));
        assertThat(root.has("library"), is(false));
        assertThat(root.path("cards").isArray(), is(true));
        assertThat(root.path("comment").asText(), equalTo("notes here"));

        JsonNode alan = null;
        for (JsonNode c : root.path("cards")) {
            assertThat(c.path("kind").asText(), anyOf(equalTo("Crypt"), equalTo("Library")));
            assertThat(c.has("printed_name"), is(true));
            if (c.path("id").asInt() == ALAN_SOVEREIGN_ADV) alan = c;
        }
        assertThat(alan, notNullValue());
        assertThat(alan.path("kind").asText(), equalTo("Crypt"));
        assertThat(alan.path("suffix").asText(), equalTo("G3 ADV"));
        assertThat(alan.path("unicity_suffix").asText(), equalTo("ADV"));
        assertThat(alan.path("types").get(0).asText(), equalTo("Vampire"));
    }

    @Test
    void normalizerReadsTheV5FormWeWrite() {
        String v5 = KrcgV5Mapper.toJson(sampleDeck());
        assertThat(KrcgV5Mapper.looksLikeV5(v5.strip()), is(true));

        Deck back = DeckNormalizer.normalize(v5);
        assertThat(back.getName(), equalTo("My Deck"));
        assertThat(back.getComments(), equalTo("notes here"));
        assertThat(back.getCrypt().getCount(), equalTo(6));
        assertThat(back.getLibrary().getCount(), equalTo(18));
    }

    @Test
    void preservesUnknownCardIds() {
        Deck deck = new Deck();
        deck.getCrypt().getCards().add(new CardCount(1, "Ghost Vampire", 2, null));
        deck.getCrypt().getCards().add(new CardCount(cardId("Nkechi"), "Nkechi", 4, null));

        Deck back = KrcgV5Mapper.fromJson(KrcgV5Mapper.toJson(deck));

        assertThat(back.getCrypt().getCards().stream().map(CardCount::getName).toList(),
                containsInAnyOrder("Ghost Vampire", "Nkechi"));
        assertThat(DeckParser.analyze(back).getErrors(), contains(containsString("Ghost Vampire")));
    }
}
