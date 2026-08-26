package net.deckserver.services;

import net.deckserver.game.enums.PlayerRole;
import net.deckserver.storage.json.system.PlayerInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class PlayerServiceTest {

    @Test
    void loadsFixturePlayersFromJpa() {
        assertThat(PlayerService.existsPlayer("Player1"), is(true));
        assertThat(PlayerService.existsPlayer("NoSuchPlayer"), is(false));
    }

    @Test
    void registersAndAuthenticatesNewPlayer() {
        String name = "PlayerServiceTestUser";
        assertThat(PlayerService.registerPlayer(name, "secret", "user@example.com"), is(true));
        assertThat(PlayerService.existsPlayer(name), is(true));
        assertThat(PlayerService.authenticate(name, "secret"), is(true));
        assertThat(PlayerService.authenticate(name, "wrong"), is(false));
        // duplicate registration is rejected
        assertThat(PlayerService.registerPlayer(name, "other", "other@example.com"), is(false));

        PlayerService.remove(name);
        assertThat(PlayerService.existsPlayer(name), is(false));
    }

    @Test
    void updatesProfileFields() {
        PlayerService.updateProfile("Player2", "player2@example.com", "discord#1", "12345", "US");
        PlayerInfo info = PlayerService.get("Player2");
        assertThat(info.getEmail(), equalTo("player2@example.com"));
        assertThat(info.getDiscordId(), equalTo("discord#1"));
        assertThat(info.getVeknId(), equalTo("12345"));
        assertThat(info.getCountryCode(), equalTo("US"));
    }

    @Test
    void togglesRoles() {
        PlayerService.setRole("Player3", PlayerRole.ADMIN, true);
        assertThat(PlayerService.get("Player3").getRoles(), hasItem(PlayerRole.ADMIN));

        PlayerService.setRole("Player3", PlayerRole.ADMIN, false);
        assertThat(PlayerService.get("Player3").getRoles(), not(hasItem(PlayerRole.ADMIN)));
    }

    @Test
    void togglesPreferences() {
        PlayerService.setImageTooltipPreference("Player4", false);
        assertThat(PlayerService.get("Player4").isShowImages(), is(false));

        PlayerService.setEdgeColor("Player4", "#123456");
        assertThat(PlayerService.get("Player4").getEdgeColor(), equalTo("#123456"));

        PlayerService.setNotificationPreference("Player4", true);
        assertThat(PlayerService.get("Player4").isNotificationsEnabled(), is(true));
    }
}
