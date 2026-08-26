package net.deckserver.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class PlayerGameActivityServiceTest {

    @Test
    void pingsAndClearsAPlayer() {
        String gameName = "PlayerGameActivityServiceTestGame";
        assertThat(PlayerGameActivityService.isPlayerPinged("Player1", gameName), is(false));

        PlayerGameActivityService.pingPlayer("Player1", gameName);
        assertThat(PlayerGameActivityService.isPlayerPinged("Player1", gameName), is(true));

        PlayerGameActivityService.clearPing("Player1", gameName);
        assertThat(PlayerGameActivityService.isPlayerPinged("Player1", gameName), is(false));
    }

    @Test
    void clearGameRemovesInMemoryEntry() {
        String gameName = "PlayerGameActivityServiceTestGame2";
        PlayerGameActivityService.recordPlayerAccess("Player1", gameName);
        assertThat(PlayerGameActivityService.getGameTimestamps(), hasKey(gameName));

        PlayerGameActivityService.clearGame(gameName);
        assertThat(PlayerGameActivityService.getGameTimestamps(), not(hasKey(gameName)));
    }
}
