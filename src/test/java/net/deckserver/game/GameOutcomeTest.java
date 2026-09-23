package net.deckserver.game;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GameOutcomeTest {

    private static Map<String, Double> vps(Object... nameThenVp) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < nameThenVp.length; i += 2) {
            map.put((String) nameThenVp[i], ((Number) nameThenVp[i + 1]).doubleValue());
        }
        return map;
    }

    @Test
    void uniqueLeaderAtOrAboveThresholdWins() {
        assertThat(GameOutcome.gameWinner(vps("A", 3.0, "B", 2.0, "C", 0.0)).orElse(null), is("A"));
    }

    @Test
    void lowerScoringQualifierDoesNotUnseatTheLeader() {
        // Regression: the old inline logic cleared the winner whenever a later
        // player had >= 2 VP but fewer than the leader.
        assertThat(GameOutcome.gameWinner(vps("A", 3.0, "B", 2.0)).orElse(null), is("A"));
    }

    @Test
    void lateLowScorerAfterATieDoesNotBecomeWinner() {
        // Regression: A=3, B=3 tie then C=2 previously handed C the game win.
        assertThat(GameOutcome.gameWinner(vps("A", 3.0, "B", 3.0, "C", 2.0)).isPresent(), is(false));
    }

    @Test
    void tieForTheLeadHasNoWinner() {
        assertThat(GameOutcome.gameWinner(vps("A", 2.5, "B", 2.5, "C", 1.0)).isPresent(), is(false));
    }

    @Test
    void tieBrokenByAStrictlyHigherLaterScoreStillResolves() {
        assertThat(GameOutcome.gameWinner(vps("A", 3.0, "B", 3.0, "C", 4.0)).orElse(null), is("C"));
    }

    @Test
    void belowThresholdNeverWinsEvenAsSoleHighScorer() {
        assertThat(GameOutcome.gameWinner(vps("A", 1.5, "B", 1.0, "C", 0.5)).isPresent(), is(false));
    }

    @Test
    void resultIsIndependentOfIterationOrder() {
        assertThat(GameOutcome.gameWinner(vps("C", 2.0, "A", 4.0, "B", 2.0)).orElse(null), is("A"));
        assertThat(GameOutcome.gameWinner(vps("B", 2.0, "C", 2.0, "A", 4.0)).orElse(null), is("A"));
    }

    @Test
    void noPlayersNoWinner() {
        assertThat(GameOutcome.gameWinner(vps()).isPresent(), is(false));
    }

    @Test
    void victoryPointTotalUpToPlayerCountIsPlausible() {
        assertThat(GameOutcome.isPlausibleVictoryPointTotal(5, 5.0), is(true));
        assertThat(GameOutcome.isPlausibleVictoryPointTotal(5, 4.5), is(true));
        assertThat(GameOutcome.isPlausibleVictoryPointTotal(5, 0.0), is(true));
    }

    @Test
    void victoryPointTotalAbovePlayerCountIsImplausible() {
        // e.g. two players each recorded with 3 VP in a 5-player game
        assertThat(GameOutcome.isPlausibleVictoryPointTotal(5, 6.0), is(false));
        assertThat(GameOutcome.isPlausibleVictoryPointTotal(4, 4.5), is(false));
    }

    @Test
    void negativeVictoryPointTotalIsImplausible() {
        assertThat(GameOutcome.isPlausibleVictoryPointTotal(5, -0.5), is(false));
    }
}
