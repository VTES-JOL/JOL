package net.deckserver.services;

import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class HistoryServiceTest {

    private static PlayerResult result(String player, double vp, boolean gameWin) {
        PlayerResult result = new PlayerResult();
        result.setPlayerName(player);
        result.setDeckName(player + "'s Deck");
        result.setVictoryPoints(vp);
        result.setGameWin(gameWin);
        return result;
    }

    @Test
    void addGame_isReadableBackFromGetHistoryAndGetGames() {
        // Truncate to millis: the recorded-at value is the map key, and it
        // round-trips through an H2 TIMESTAMP WITH TIME ZONE column whose
        // fractional-second precision is coarser than a Linux JVM's
        // OffsetDateTime.now() (nanos). Without this the reconstructed key
        // fails to equal `timestamp` on CI, though it happens to match on a
        // macOS clock. Production never looks history up by a client-held key.
        OffsetDateTime timestamp = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        GameHistory history = new GameHistory();
        history.setName("HistoryServiceTest Game");
        history.setStarted("2026-01-01T00:00:00Z");
        history.setEnded("2026-01-01T01:00:00Z");
        history.setResults(List.of(result("Player1", 2.0, true), result("Player2", 1.0, false)));

        HistoryService.addGame(timestamp, history);

        assertThat(HistoryService.getHistory().get(timestamp).getName(), equalTo("HistoryServiceTest Game"));
        assertThat(HistoryService.getGames(), hasItem(hasProperty("name", equalTo("HistoryServiceTest Game"))));
    }

    @Test
    void validateGW_promotesTheHighestVpPlayerToGameWinWhenNoneWasRecorded() {
        // Truncate to millis: the recorded-at value is the map key, and it
        // round-trips through an H2 TIMESTAMP WITH TIME ZONE column whose
        // fractional-second precision is coarser than a Linux JVM's
        // OffsetDateTime.now() (nanos). Without this the reconstructed key
        // fails to equal `timestamp` on CI, though it happens to match on a
        // macOS clock. Production never looks history up by a client-held key.
        OffsetDateTime timestamp = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        GameHistory history = new GameHistory();
        history.setName("HistoryServiceTest ValidateGW Game");
        history.setResults(List.of(result("Player1", 2.5, false), result("Player2", 1.0, false)));
        HistoryService.addGame(timestamp, history);

        HistoryService.validateGW();

        GameHistory updated = HistoryService.getHistory().get(timestamp);
        PlayerResult winner = updated.getResults().stream()
                .filter(r -> r.getPlayerName().equals("Player1")).findFirst().orElseThrow();
        assertThat(winner.isGameWin(), is(true));
    }
}
