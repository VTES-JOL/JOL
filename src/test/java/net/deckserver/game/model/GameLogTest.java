package net.deckserver.game.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GameLogTest {

    @Test
    void sentenceTrimsAndAddsTerminalPunctuation() {
        assertThat(GameLog.sentence("  draws a card from their library "), is("draws a card from their library."));
    }

    @Test
    void sentenceCollapsesInternalWhitespace() {
        assertThat(GameLog.sentence("clears  \t the path"), is("clears the path."));
    }

    @Test
    void sentenceKeepsAnExistingTerminator() {
        assertThat(GameLog.sentence("rolls 1–6: 4"), is("rolls 1–6: 4."));
        assertThat(GameLog.sentence("is it contested?"), is("is it contested?"));
        assertThat(GameLog.sentence("ousted!"), is("ousted!"));
    }

    @Test
    void sentenceHandlesEmptyAndNull() {
        assertThat(GameLog.sentence(""), is(""));
        assertThat(GameLog.sentence("   "), is(""));
        assertThat(GameLog.sentence(null), is(""));
    }

    @Test
    void possessiveCollapsesToTheirForTheActor() {
        assertThat(GameLog.possessive("Alice", "Alice"), is("their"));
        assertThat(GameLog.possessive("Bob", "Alice"), is("Bob's"));
    }

    @Test
    void pluralAgreesWithCount() {
        assertThat(GameLog.plural(1, "card"), is("1 card"));
        assertThat(GameLog.plural(0, "card"), is("0 cards"));
        assertThat(GameLog.plural(3, "vote"), is("3 votes"));
    }
}
