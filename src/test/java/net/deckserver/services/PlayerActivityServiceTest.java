package net.deckserver.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class PlayerActivityServiceTest {

    @Test
    void recordsAndReadsBackPlayerAccess() {
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        PlayerActivityService.recordPlayerAccess("Player1");
        assertThat(PlayerActivityService.getPlayerAccess("Player1"), greaterThan(before));
    }

    @Test
    void defaultsToEpochForUnknownPlayer() {
        assertThat(PlayerActivityService.getPlayerAccess("NoSuchPlayer").getYear(), equalTo(2000));
    }
}
