package net.deckserver.services;

import net.deckserver.game.enums.GameFormat;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.DeckValidity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class DeckValidityServiceTest {

    @Test
    void computesAndPersistsAllValidatedFormats() {
        DeckValidityService.computeAndPersist("validity-empty-deck", new Deck());

        Map<GameFormat, DeckValidity> validity = DeckValidityService.getValidity("validity-empty-deck");

        assertThat(validity.keySet(), containsInAnyOrder(GameFormat.STANDARD, GameFormat.DUEL, GameFormat.V5));
        // An empty deck is invalid for every format, with at least one error each.
        validity.forEach((format, v) -> {
            assertThat(format + " should be invalid", v.valid(), is(false));
            assertThat(format + " should list errors", v.errors(), is(not(empty())));
            assertThat(v.computedAt(), notNullValue());
        });
    }

    @Test
    void validFlagAlwaysMatchesErrorEmptiness() {
        Deck deck = DeckService.getDeck("01GR3EV0YN7R2GP2E0NQBTK8JB").getDeck(); // fixture: "Weenie Animalism"
        DeckValidityService.computeAndPersist("validity-fixture-deck", deck);

        DeckValidityService.getValidity("validity-fixture-deck").forEach((format, v) ->
                assertThat(format + ": valid iff no errors", v.valid(), equalTo(v.errors().isEmpty())));
    }

    @Test
    void singleFormatLookup() {
        DeckValidityService.computeAndPersist("validity-single", new Deck());

        assertThat(DeckValidityService.getValidity("validity-single", GameFormat.STANDARD).isPresent(), is(true));
        assertThat(DeckValidityService.getValidity("validity-single", GameFormat.PLAYTEST).isPresent(), is(false));
    }

    @Test
    void recomputeUpsertsInPlace() {
        DeckValidityService.computeAndPersist("validity-upsert", new Deck());
        DeckValidity first = DeckValidityService.getValidity("validity-upsert", GameFormat.STANDARD).orElseThrow();

        DeckValidityService.computeAndPersist("validity-upsert", new Deck());
        Map<GameFormat, DeckValidity> after = DeckValidityService.getValidity("validity-upsert");

        assertThat("still one row per format", after.keySet(), hasSize(3));
        assertThat(after.get(GameFormat.STANDARD).computedAt(),
                greaterThanOrEqualTo(first.computedAt()));
    }

    @Test
    void unknownDeckHasNoValidity() {
        assertThat(DeckValidityService.getValidity("no-such-deck"), anEmptyMap());
        assertThat(DeckValidityService.getValidity("no-such-deck", GameFormat.STANDARD).isPresent(), is(false));
    }

    @Test
    void nullArgumentsAreIgnored() {
        DeckValidityService.computeAndPersist(null, new Deck());
        DeckValidityService.computeAndPersist("x", null);
        assertThat(DeckValidityService.getValidity("x"), anEmptyMap());
    }
}
