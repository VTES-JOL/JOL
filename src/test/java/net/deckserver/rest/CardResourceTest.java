package net.deckserver.rest;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.rest.bean.CardBean;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain unit test for the text-mode card endpoints — {@link CardResource#card}
 * / {@link CardResource#cards} are thin wrappers over {@link CardRegistry}
 * (static, loads {@code csv/core/*.csv} from the working dir, no DB / no
 * Quarkus runtime), and the methods under test don't touch the injected
 * {@code SecurityContext} / {@code HttpHeaders}.
 */
class CardResourceTest {

    private final CardResource resource = new CardResource();

    private static String anyCryptId() {
        return CardRegistry.allCards().stream()
                .filter(Card::isCrypt)
                .map(Card::id)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void singleCard_libraryCard_hasTextAndCostAndNoCryptFields() {
        Response response = resource.card("100001"); // .44 Magnum
        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString("Cache-Control"), containsString("immutable"));

        CardBean bean = (CardBean) response.getEntity();
        assertThat(bean.id(), is("100001"));
        assertThat(bean.name(), is(".44 Magnum"));
        assertThat(bean.crypt(), is(false));
        assertThat(bean.types(), contains("Equipment"));
        assertThat(bean.typeLine(), is("Equipment"));
        assertThat(bean.cardText(), not(emptyOrNullString()));
        assertThat(bean.poolCost(), is(2));
        // crypt-only fields are null on a library card
        assertThat(bean.clan(), nullValue());
        assertThat(bean.capacity(), nullValue());
        assertThat(bean.disciplines(), nullValue());
    }

    @Test
    void singleCard_cryptCard_hasCapacityAndNoLibraryFields() {
        CardBean bean = (CardBean) resource.card(anyCryptId()).getEntity();
        assertThat(bean.crypt(), is(true));
        assertThat(bean.types(), either(contains("Vampire")).or(contains("Imbued")));
        assertThat(bean.capacity(), notNullValue());
        assertThat(bean.disciplines(), notNullValue());
        assertThat(bean.advanced(), notNullValue());
        // library-only fields are null on a crypt card
        assertThat(bean.poolCost(), nullValue());
        assertThat(bean.andDisciplines(), nullValue());
        assertThat(bean.doNotReplace(), nullValue());
    }

    @Test
    void singleCard_unknownId_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> resource.card("no-such-card-id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void batch_resolvesKnownIdsInOrderAndDropsUnknown() {
        String cryptId = anyCryptId();
        Response response = resource.cards("100001, no-such, " + cryptId);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString("Cache-Control"), containsString("immutable"));

        List<CardBean> beans = (List<CardBean>) response.getEntity();
        assertThat(beans, hasSize(2));
        assertThat(beans.get(0).id(), is("100001"));
        assertThat(beans.get(1).id(), is(cryptId));
        assertThat(beans.get(1).crypt(), is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    void batch_blankIdsReturnsEmpty() {
        List<CardBean> beans = (List<CardBean>) resource.cards("").getEntity();
        assertThat(beans, is(empty()));
    }
}
