package net.deckserver.services;

import net.deckserver.storage.json.system.RefreshTokenInfo;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class RefreshTokenServiceTest {

    @Test
    void issue_recordsOneTokenForPlayer_withGivenDeviceAndRememberFlag() {
        String player = uniquePlayer();

        RefreshTokenService.issue(player, "JUnit-Agent", true);

        List<RefreshTokenInfo> tokens = RefreshTokenService.list(player);
        assertThat(tokens, hasSize(1));
        assertThat(tokens.get(0).getDeviceLabel(), is("JUnit-Agent"));
        assertThat(tokens.get(0).isRemember(), is(true));
    }

    @Test
    void validateAndRotate_succeeds_andRotatesSecret_preservingPlayerAndRememberFlag() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);

        Optional<RefreshTokenService.Rotated> rotated = RefreshTokenService.validateAndRotate(issued.cookieValue());

        assertThat(rotated.isPresent(), is(true));
        assertThat(rotated.get().playerName(), is(player));
        assertThat(rotated.get().remember(), is(true));
        assertThat("rotation must issue a new secret", rotated.get().cookieValue(), not(equalTo(issued.cookieValue())));
        assertThat("rotated value must keep the same token id", idOf(rotated.get().cookieValue()), is(idOf(issued.cookieValue())));
    }

    @Test
    void validateAndRotate_allowsChainedUse_ofEachNewlyRotatedValue() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", false);

        RefreshTokenService.Rotated first = RefreshTokenService.validateAndRotate(issued.cookieValue()).orElseThrow();
        RefreshTokenService.Rotated second = RefreshTokenService.validateAndRotate(first.cookieValue()).orElseThrow();

        assertThat(second.playerName(), is(player));
        assertThat(second.cookieValue(), not(equalTo(first.cookieValue())));
    }

    @Test
    void validateAndRotate_returnsEmpty_forUnknownId() {
        assertThat(RefreshTokenService.validateAndRotate(UUID.randomUUID() + ".some-secret"), is(Optional.empty()));
    }

    @Test
    void validateAndRotate_returnsEmpty_whenCookieValueHasNoSeparator() {
        assertThat(RefreshTokenService.validateAndRotate("no-dot-in-here"), is(Optional.empty()));
    }

    @Test
    void validateAndRotate_returnsEmpty_forNullCookieValue() {
        assertThat(RefreshTokenService.validateAndRotate(null), is(Optional.empty()));
    }

    @Test
    void validateAndRotate_detectsReplayOfStaleSecret_andRevokesToken() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        // legitimate use rotates the secret away from the originally issued value
        RefreshTokenService.validateAndRotate(issued.cookieValue()).orElseThrow();

        // an attacker (or a lost race) replays the now-stale original secret
        Optional<RefreshTokenService.Rotated> replay = RefreshTokenService.validateAndRotate(issued.cookieValue());

        assertThat(replay, is(Optional.empty()));
        assertThat("the whole token id should be revoked once replay is detected, not just left rotated",
                RefreshTokenService.list(player), is(empty()));
    }

    @Test
    void validateAndRotate_returnsEmpty_andRemovesRow_whenTokenExpired() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        forceExpire(player);

        Optional<RefreshTokenService.Rotated> rotated = RefreshTokenService.validateAndRotate(issued.cookieValue());

        assertThat(rotated, is(Optional.empty()));
        assertThat(RefreshTokenService.list(player), is(empty()));
    }

    @Test
    void validateAndRotate_capsExpiryAtAbsoluteMaxAge_evenForRememberedTokens() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        long now = System.currentTimeMillis();
        long farPastCreatedAt = now - TimeUnit.DAYS.toMillis(89);
        RefreshTokenService.list(player).get(0).setCreatedAt(farPastCreatedAt);

        RefreshTokenService.validateAndRotate(issued.cookieValue()).orElseThrow();

        long newExpiresAt = RefreshTokenService.list(player).get(0).getExpiresAt();
        long absoluteCap = farPastCreatedAt + TimeUnit.DAYS.toMillis(90);
        long uncappedRememberExpiry = now + TimeUnit.DAYS.toMillis(30);
        assertThat("expiry must be capped at 90 days from original creation",
                newExpiresAt, lessThanOrEqualTo(absoluteCap));
        assertThat("cap should actually be the limiting factor here, not the 30-day remember TTL",
                newExpiresAt, lessThan(uncappedRememberExpiry));
    }

    @Test
    void revoke_byCookieValue_removesToken() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);

        RefreshTokenService.revoke(issued.cookieValue());

        assertThat(RefreshTokenService.list(player), is(empty()));
        assertThat(RefreshTokenService.validateAndRotate(issued.cookieValue()), is(Optional.empty()));
    }

    @Test
    void revoke_byPlayerAndId_removesOnlyThatDevice() {
        String player = uniquePlayer();
        RefreshTokenService.Issued deviceA = RefreshTokenService.issue(player, "Device A", true);
        RefreshTokenService.Issued deviceB = RefreshTokenService.issue(player, "Device B", true);

        RefreshTokenService.revoke(player, deviceA.id());

        List<RefreshTokenInfo> remaining = RefreshTokenService.list(player);
        assertThat(remaining, hasSize(1));
        assertThat(remaining.get(0).getId(), is(deviceB.id()));
    }

    @Test
    void revokeAll_removesEveryTokenForPlayer_butNotOtherPlayers() {
        String player = uniquePlayer();
        String otherPlayer = uniquePlayer();
        RefreshTokenService.issue(player, "Device A", true);
        RefreshTokenService.issue(player, "Device B", true);
        RefreshTokenService.Issued untouched = RefreshTokenService.issue(otherPlayer, "Device C", true);

        RefreshTokenService.revokeAll(player);

        assertThat(RefreshTokenService.list(player), is(empty()));
        assertThat(RefreshTokenService.list(otherPlayer), hasSize(1));
        assertThat(RefreshTokenService.validateAndRotate(untouched.cookieValue()).isPresent(), is(true));
    }

    @Test
    void list_returnsEmpty_forPlayerWithNoTokens() {
        assertThat(RefreshTokenService.list(uniquePlayer()), is(empty()));
    }

    @Test
    void cleanupExpired_removesOnlyExpiredTokens() {
        String player = uniquePlayer();
        RefreshTokenService.Issued expired = RefreshTokenService.issue(player, "Old Device", true);
        RefreshTokenService.Issued fresh = RefreshTokenService.issue(player, "New Device", true);
        forceExpire(player, expired.id());

        RefreshTokenService.cleanupExpired();

        List<RefreshTokenInfo> remaining = RefreshTokenService.list(player);
        assertThat(remaining, hasSize(1));
        assertThat(remaining.get(0).getId(), is(fresh.id()));
    }

    private static void forceExpire(String player) {
        long past = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
        for (RefreshTokenInfo info : RefreshTokenService.list(player)) {
            info.setExpiresAt(past);
        }
    }

    private static void forceExpire(String player, String id) {
        long past = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
        for (RefreshTokenInfo info : RefreshTokenService.list(player)) {
            if (info.getId().equals(id)) {
                info.setExpiresAt(past);
            }
        }
    }

    private static String idOf(String cookieValue) {
        return cookieValue.substring(0, cookieValue.indexOf('.'));
    }

    private static String uniquePlayer() {
        return "RefreshTokenServiceTest-" + UUID.randomUUID();
    }
}
