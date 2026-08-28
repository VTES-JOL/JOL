package net.deckserver.game.cards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Verifies the ported {@link CardRegistry} loads the VEKN CSVs correctly and
 * agrees with the pre-built {@code static/secured/cards.json} that
 * {@link net.deckserver.services.CardService} serves — the two must stay in
 * lock-step while both are live.
 *
 * <p>Plain unit test: the registry has no DB / CDI / test-mode dependency, and
 * loads {@code csv/core/*.csv} relative to the working directory (repo root
 * under Maven).
 */
class CardRegistryTest {

    /** and/or discipline codes are always the 3-letter code, or "flight". */
    private static final Pattern LIB_DISC_CODE = Pattern.compile("[a-z]{3}|flight");
    /** crypt discipline tokens are 3 letters, case marking sup/inf, or "flight". */
    private static final Pattern CRYPT_DISC_CODE = Pattern.compile("[a-zA-Z]{3}|flight|FLIGHT");

    @Test
    void loadsAPlausibleNumberOfCards() {
        assertThat(CardRegistry.allCards(), hasSize(greaterThan(3000)));
    }

    @Test
    void parsesAKnownCryptCard() {
        Card card = CardRegistry.findById("200001");
        assertThat(card, instanceOf(CryptCard.class));
        CryptCard vampire = (CryptCard) card;
        assertThat(vampire.name(), equalTo("Aabbt Kindred"));
        assertThat(vampire.type(), equalTo(CryptType.VAMPIRE));
        assertThat(vampire.clan(), equalTo("Follower of Set"));
        assertThat(vampire.group(), equalTo("2"));
        assertThat(vampire.capacity(), equalTo(4));
        assertThat(vampire.advanced(), is(false));
        assertThat(vampire.disciplines(), containsInAnyOrder("for", "pre", "ser"));
    }

    @Test
    void parsesAKnownLibraryCard() {
        Card card = CardRegistry.findById("100001");
        assertThat(card, instanceOf(LibraryCard.class));
        LibraryCard equipment = (LibraryCard) card;
        assertThat(equipment.name(), equalTo(".44 Magnum"));
        assertThat(equipment.types(), contains("Equipment"));
        assertThat(equipment.poolCost(), equalTo(2));
        assertThat(equipment.bloodCost(), nullValue());
        assertThat(equipment.andDisciplines(), empty());
        assertThat(equipment.orDisciplines(), empty());
        assertThat(equipment.burnOption(), is(false));
    }

    @Test
    void everyLibraryDisciplineResolvesToACode() {
        List<String> unmapped = new ArrayList<>();
        for (Card card : CardRegistry.allCards()) {
            if (!(card instanceof LibraryCard lib)) continue;
            for (String code : concat(lib.andDisciplines(), lib.orDisciplines())) {
                if (!LIB_DISC_CODE.matcher(code).matches()) {
                    unmapped.add(lib.name() + " -> '" + code + "'");
                }
            }
        }
        assertThat("library discipline names that did not map to a code: " + unmapped, unmapped, empty());
    }

    @Test
    void everyCryptDisciplineIsACode() {
        List<String> odd = new ArrayList<>();
        for (Card card : CardRegistry.allCards()) {
            if (!(card instanceof CryptCard crypt)) continue;
            for (String code : crypt.disciplines()) {
                if (!CRYPT_DISC_CODE.matcher(code).matches()) {
                    odd.add(crypt.name() + " -> '" + code + "'");
                }
            }
        }
        assertThat("crypt discipline tokens that are not codes: " + odd, odd, empty());
    }

    @Test
    void bareNameLookupResolvesUnambiguousVampires() {
        Card card = CardRegistry.findByNormalizedName("aabbt kindred");
        assertThat(card, instanceOf(CryptCard.class));
        assertThat(card.id(), equalTo("200001"));
    }

    @Test
    void everyCardInCardsJsonResolvesInTheRegistry() throws IOException {
        JsonNode cardsJson = new ObjectMapper().readTree(new File("static/secured/cards.json"));
        assertThat("static/secured/cards.json should be a non-empty array", cardsJson.isArray() && cardsJson.size() > 0, is(true));

        Set<String> jsonIds = new HashSet<>();
        List<String> missing = new ArrayList<>();
        for (JsonNode card : cardsJson) {
            String id = card.get("id").asText();
            jsonIds.add(id);
            if (CardRegistry.findById(id) == null) {
                missing.add(id + " (" + card.path("name").asText() + ")");
            }
        }

        // Informational: cards the CSVs carry that the built JSON does not.
        // Not asserted — the CSVs can legitimately be ahead of a stale build.
        long registryOnly = CardRegistry.allCards().stream()
                .map(Card::id)
                .filter(id -> !jsonIds.contains(id))
                .count();
        System.out.printf("CardRegistry reconciliation: %d in cards.json, %d in registry, %d registry-only%n",
                jsonIds.size(), CardRegistry.allCards().size(), registryOnly);

        assertThat("cards.json ids missing from CardRegistry: "
                + missing.subList(0, Math.min(missing.size(), 20)), missing, empty());
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> out = new ArrayList<>(a);
        out.addAll(b);
        return out;
    }
}
