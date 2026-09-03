package net.deckserver.services;

import net.deckserver.rest.bean.CardDetailBean;
import net.deckserver.rest.bean.EnrichedDeck;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.Crypt;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.Library;
import net.deckserver.storage.json.deck.LibraryCard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Plain unit test — {@link DeckEnrichmentService} projects over the static
 * {@link net.deckserver.game.cards.CardRegistry} (CSV-backed, no DB).
 */
class DeckEnrichmentServiceTest {

    private static String idOf(String name, boolean crypt) {
        return CardSearchService.autocomplete(name).stream()
                .filter(c -> c.crypt() == crypt)
                .filter(c -> c.name().equalsIgnoreCase(name))
                .map(CardDetailBean::id)
                .findFirst()
                .orElseThrow(() -> new AssertionError("card not found: " + name));
    }

    private static Deck deckOf(String cryptId, String libraryId) {
        Deck deck = new Deck();
        Crypt crypt = new Crypt();
        crypt.setCards(List.of(new CardCount(Integer.valueOf(cryptId), "crypt", 4, "")));
        crypt.setCount(4);
        deck.setCrypt(crypt);
        Library library = new Library();
        LibraryCard section = new LibraryCard();
        section.setType("Action");
        section.setCards(List.of(new CardCount(Integer.valueOf(libraryId), "lib", 6, "")));
        library.setCards(List.of(section));
        library.setCount(6);
        deck.setLibrary(library);
        return deck;
    }

    @Test
    void enrichesEveryDistinctCardWithDisplayDetail() {
        String cryptId = idOf("Aabbt Kindred", true);
        String libraryId = idOf("Govern the Unaligned", false);

        EnrichedDeck enriched = DeckEnrichmentService.enrich(deckOf(cryptId, libraryId));

        Map<String, CardDetailBean> details = enriched.details();
        assertThat(details.keySet(), containsInAnyOrder(cryptId, libraryId));

        CardDetailBean crypt = details.get(cryptId);
        assertThat(crypt.crypt(), is(true));
        assertThat(crypt.capacity(), equalTo(4));
        assertThat(crypt.disciplines(), is(not(empty())));

        CardDetailBean library = details.get(libraryId);
        assertThat(library.crypt(), is(false));
        assertThat(library.types(), is(not(empty())));
        // Govern the Unaligned requires Dominate.
        assertThat(library.orDisciplines(), hasItem(equalToIgnoringCase("dom")));
    }

    @Test
    void nullDeckYieldsEmptyDetails() {
        EnrichedDeck enriched = DeckEnrichmentService.enrich(null);
        assertThat(enriched.deck(), nullValue());
        assertThat(enriched.details(), is(anEmptyMap()));
    }

    @Test
    void unknownCardIdIsSkippedNotThrown() {
        EnrichedDeck enriched = DeckEnrichmentService.enrich(deckOf("999999999", "999999998"));
        assertThat(enriched.details(), is(anEmptyMap()));
    }
}
