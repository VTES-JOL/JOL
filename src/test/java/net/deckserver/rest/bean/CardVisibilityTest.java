package net.deckserver.rest.bean;

import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.PlayerData;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CardVisibilityTest {

    private static final PlayerData ALICE = new PlayerData("Alice");
    private static final PlayerData BOB = new PlayerData("Bob");

    private static CardData card(PlayerData owner, boolean faceDown) {
        CardData c = new CardData("1234", owner);
        c.setFaceDown(faceDown);
        return c;
    }

    @Test
    void faceDownCardIsVisibleToItsController() {
        // regionOwner == viewer, i.e. the card sits on the viewer's own board
        assertThat(CardVisibility.visibleTo(card(ALICE, true), "Alice", "Alice", true), is(true));
    }

    @Test
    void faceDownCardIsHiddenFromEveryoneElseEvenInAVisibleRegion() {
        assertThat(CardVisibility.visibleTo(card(ALICE, true), "Alice", "Bob", true), is(false));
    }

    @Test
    void faceDownBeatsTheForeignCardOverride() {
        // Bob's card, face down on Alice's board: still hidden from Bob (and all)
        assertThat(CardVisibility.visibleTo(card(BOB, true), "Alice", "Bob", true), is(false));
        assertThat(CardVisibility.visibleTo(card(BOB, true), "Alice", "Carol", true), is(false));
    }

    @Test
    void nonFaceDownCardInAVisibleRegionIsVisibleToAll() {
        assertThat(CardVisibility.visibleTo(card(ALICE, false), "Alice", "Bob", true), is(true));
    }

    @Test
    void nonFaceDownOwnCardInAHiddenRegionIsNotVisibleToOthers() {
        assertThat(CardVisibility.visibleTo(card(ALICE, false), "Alice", "Bob", false), is(false));
    }

    @Test
    void nonFaceDownForeignCardInAHiddenRegionIsStillShown() {
        // Bob's stolen vampire in Alice's (hidden) region — the foreign-card override
        assertThat(CardVisibility.visibleTo(card(BOB, false), "Alice", "Carol", false), is(true));
    }
}
