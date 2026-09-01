package net.deckserver.game.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GameNamesTest {

    @Test
    void tournamentRoundTablesAreTournaments() {
        assertThat(GameNames.isTournament("Test - Finals Seeding: Round 1 - Table 1"), is(true));
        assertThat(GameNames.isTournament("Rounds are being played: Round 12 - Table 3"), is(true));
        assertThat(GameNames.isTournament("Round 2  -  Table 10"), is(true));
    }

    @Test
    void finalTableIsATournament() {
        assertThat(GameNames.isTournament("Some Event Final Table"), is(true));
    }

    @Test
    void casualGamesAreNotTournaments() {
        // Real prod game names, including ones with commas that a naive CSV split would mangle.
        assertThat(GameNames.isTournament("Anson, Prince of Seattle"), is(false));
        assertThat(GameNames.isTournament("need help to learn JOL, new players welcome"), is(false));
        assertThat(GameNames.isTournament("v5 or swap 12"), is(false));
        assertThat(GameNames.isTournament("Round table discussion"), is(false));
        assertThat(GameNames.isTournament("Table 4 chat"), is(false));
    }

    @Test
    void nullIsNotATournament() {
        assertThat(GameNames.isTournament(null), is(false));
    }
}
