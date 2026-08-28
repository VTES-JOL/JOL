package net.deckserver.services;

import net.deckserver.rest.bean.CardDetailBean;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Plain unit test — {@link CardSearchService} and {@link net.deckserver.game.cards.CardRegistry}
 * are static, load {@code csv/core/*.csv} from the working dir, no DB.
 */
class CardSearchServiceTest {

    @Test
    void autocompleteRanksExactWordPrefixFirst() {
        List<CardDetailBean> results = CardSearchService.autocomplete("govern");

        assertThat(results, is(not(empty())));
        assertThat(results.size(), lessThanOrEqualTo(5));
        assertThat(results.get(0).name(), equalTo("Govern the Unaligned"));
    }

    @Test
    void autocompleteIsEmptyForBlankQuery() {
        assertThat(CardSearchService.autocomplete(""), is(empty()));
        assertThat(CardSearchService.autocomplete("   "), is(empty()));
        assertThat(CardSearchService.autocomplete(null), is(empty()));
    }

    @Test
    void autocompleteFindsCryptCardsWithDisplayData() {
        List<CardDetailBean> results = CardSearchService.autocomplete("aabbt kindred");

        CardDetailBean crypt = results.stream().filter(CardDetailBean::crypt).findFirst().orElseThrow();
        assertThat(crypt.name(), equalTo("Aabbt Kindred"));
        assertThat(crypt.types(), contains("Vampire"));
        assertThat(crypt.group(), equalTo("2"));
        assertThat(crypt.capacity(), equalTo(4));
        assertThat(crypt.disciplines(), containsInAnyOrder("for", "pre", "ser"));
        // library-only fields are empty/null for a crypt card
        assertThat(crypt.andDisciplines(), is(empty()));
        assertThat(crypt.poolCost(), nullValue());
    }

    @Test
    void detailsProjectsLibraryCard() {
        List<CardDetailBean> details = CardSearchService.findDetailsByIds(List.of("100001")); // .44 Magnum

        assertThat(details, hasSize(1));
        CardDetailBean equipment = details.get(0);
        assertThat(equipment.crypt(), is(false));
        assertThat(equipment.types(), contains("Equipment"));
        assertThat(equipment.group(), nullValue());
        assertThat(equipment.poolCost(), equalTo(2));
        assertThat(equipment.capacity(), nullValue());
        assertThat(equipment.disciplines(), is(empty()));
    }

    @Test
    void detailsSplitsAndVersusOrDisciplines() {
        String alphaGlintId = CardSearchService.autocomplete("alpha glint").get(0).id();
        CardDetailBean andCard = CardSearchService.findDetailsByIds(List.of(alphaGlintId)).get(0); // "Animalism & Fortitude"
        assertThat(andCard.andDisciplines(), containsInAnyOrder("ani", "for"));
        assertThat(andCard.orDisciplines(), is(empty()));

        String absorbId = CardSearchService.autocomplete("absorb the mind").get(0).id();
        CardDetailBean orCard = CardSearchService.findDetailsByIds(List.of(absorbId)).get(0); // "Mytherceria/Dominate"
        assertThat(orCard.orDisciplines(), containsInAnyOrder("myt", "dom"));
        assertThat(orCard.andDisciplines(), is(empty()));
    }

    @Test
    void detailsSkipsUnknownIds() {
        assertThat(CardSearchService.findDetailsByIds(List.of("1", "999999999")), is(empty()));
        assertThat(CardSearchService.findDetailsByIds(List.of()), is(empty()));
    }
}
