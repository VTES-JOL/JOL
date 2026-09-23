package net.deckserver.services;

import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class HistoryServiceValidationTest {

    private static PlayerResult result(String name, double vp, boolean gameWin) {
        PlayerResult r = new PlayerResult();
        r.setPlayerName(name);
        r.setVictoryPoints(vp);
        r.setGameWin(gameWin);
        return r;
    }

    private static GameHistory game(PlayerResult... results) {
        GameHistory g = new GameHistory();
        g.setName("Test Game");
        g.setResults(new java.util.ArrayList<>(Arrays.asList(results)));
        return g;
    }

    @Test
    void soundRecordHasNoInvalidReason() {
        GameHistory g = game(result("A", 4.0, true), result("B", 1.0, false),
                result("C", 0.0, false), result("D", 0.0, false), result("E", 0.0, false));
        assertThat(HistoryService.invalidReason(g), is(nullValue()));
    }

    @Test
    void vpTotalAbovePlayerCountIsInvalid() {
        GameHistory g = game(result("A", 3.0, true), result("B", 3.0, false),
                result("C", 0.0, false), result("D", 0.0, false), result("E", 0.0, false));
        assertThat(HistoryService.invalidReason(g), containsString("VP total"));
    }

    @Test
    void emptyResultsAreInvalid() {
        assertThat(HistoryService.invalidReason(game()), containsString("no player results"));
    }

    @Test
    void missingPlayerNameIsInvalid() {
        GameHistory g = game(result("A", 2.0, true), result(null, 1.0, false),
                result("C", 0.0, false), result("D", 0.0, false));
        assertThat(HistoryService.invalidReason(g), containsString("player name"));
    }

    @Test
    void reconcileMovesGameWinToTheStrictLeader() {
        GameHistory g = game(result("A", 4.0, false), result("B", 1.0, true),
                result("C", 0.0, false), result("D", 0.0, false));

        HistoryService.reconcileGameWin(g);

        assertThat(g.getResults().get(0).isGameWin(), is(true));   // A
        assertThat(g.getResults().get(1).isGameWin(), is(false));  // B
    }

    @Test
    void reconcileClearsGameWinWhenTheLeadIsTied() {
        GameHistory g = game(result("A", 2.0, true), result("B", 2.0, false),
                result("C", 1.0, false), result("D", 0.0, false));

        HistoryService.reconcileGameWin(g);

        assertThat(g.getResults().stream().anyMatch(PlayerResult::isGameWin), is(false));
    }
}
