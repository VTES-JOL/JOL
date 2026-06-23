package net.deckserver.services;

import net.deckserver.game.enums.GameStatus;
import net.deckserver.storage.json.system.TournamentInviteStatus;
import net.deckserver.storage.json.system.TournamentMetadata;
import net.deckserver.storage.json.system.TournamentRegistration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TournamentLifecycleTest {

    private static final String DRAFT_TOURNAMENT = "Work in Progress Tournament Design";
    private static final String REGISTRATION_OPEN_TOURNAMENT = "Registrations Open";
    private static final String SEATING_PHASE_TOURNAMENT = "Setup Round Seating";
    private static final String ACTIVE_TOURNAMENT = "Rounds are being played";

    // --- Phase: EDIT (Draft) ---

    @Test
    @Order(1)
    void edit_tournament_is_not_visible_to_players() {
        List<String> names = TournamentService.getOpenTournaments()
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, not(hasItem(DRAFT_TOURNAMENT)));
    }

    @Test
    @Order(2)
    void edit_tournament_appears_in_admin_list_with_edit_status() {
        List<String> names = TournamentService.getTournamentsWithStatus(List.of(GameStatus.EDIT))
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, hasItem(DRAFT_TOURNAMENT));
    }

    @Test
    @Order(3)
    void edit_tournament_has_edit_status_in_metadata() {
        TournamentMetadata draft = TournamentService.getTournamentsWithStatus(
                        List.of(GameStatus.EDIT, GameStatus.STARTING, GameStatus.ACTIVE))
                .stream().filter(t -> t.getName().equals(DRAFT_TOURNAMENT)).findFirst().orElseThrow();
        assertThat(draft.getStatus(), is("EDIT"));
    }

    // --- Phase: STARTING (registration window open) ---

    @Test
    @Order(4)
    void starting_with_open_registration_is_visible_to_players() {
        List<String> names = TournamentService.getOpenTournaments()
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, hasItem(REGISTRATION_OPEN_TOURNAMENT));
    }

    @Test
    @Order(5)
    void starting_tournament_shows_player_as_registered_when_signed_up() {
        TournamentMetadata tournament = TournamentService.getOpenTournaments("Player1")
                .stream().filter(t -> t.getName().equals(REGISTRATION_OPEN_TOURNAMENT))
                .findFirst().orElseThrow();
        assertThat(tournament.isRegistered(), is(true));
    }

    @Test
    @Order(6)
    void starting_tournament_shows_unregistered_player_as_not_registered() {
        TournamentMetadata tournament = TournamentService.getOpenTournaments("Player9")
                .stream().filter(t -> t.getName().equals(REGISTRATION_OPEN_TOURNAMENT))
                .findFirst().orElseThrow();
        assertThat(tournament.isRegistered(), is(false));
    }

    @Test
    @Order(7)
    void registered_tournaments_includes_open_tournament_for_registered_player() {
        List<String> names = TournamentService.getRegisteredTournaments("Player1")
                .stream().map(TournamentInviteStatus::getName).toList();
        assertThat(names, hasItem(REGISTRATION_OPEN_TOURNAMENT));
    }

    @Test
    @Order(8)
    void registered_tournaments_excludes_tournament_for_unregistered_player() {
        List<String> names = TournamentService.getRegisteredTournaments("Player9")
                .stream().map(TournamentInviteStatus::getName).toList();
        assertThat(names, not(hasItem(REGISTRATION_OPEN_TOURNAMENT)));
    }

    @Test
    @Order(9)
    void open_registration_tournament_has_all_registered_players() {
        List<TournamentRegistration> registrations = TournamentService.getRegistrations(REGISTRATION_OPEN_TOURNAMENT);
        assertThat(registrations, hasSize(8));
        assertThat(registrations.stream().map(TournamentRegistration::getPlayer).toList(),
                hasItems("Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7", "Player8"));
    }

    // --- Phase: STARTING (registration closed, seating phase) ---

    @Test
    @Order(10)
    void starting_with_closed_registration_not_visible_to_players() {
        List<String> names = TournamentService.getOpenTournaments()
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, not(hasItem(SEATING_PHASE_TOURNAMENT)));
    }

    @Test
    @Order(11)
    void starting_with_closed_registration_excluded_from_registered_view() {
        List<String> names = TournamentService.getRegisteredTournaments("Player1")
                .stream().map(TournamentInviteStatus::getName).toList();
        assertThat(names, not(hasItem(SEATING_PHASE_TOURNAMENT)));
    }

    @Test
    @Order(12)
    void get_tournament_ready_to_start_finds_starting_tournament_regardless_of_play_dates() {
        TournamentMetadata meta = TournamentService.getTournamentReadyToStart(SEATING_PHASE_TOURNAMENT);
        assertThat(meta.getName(), is(SEATING_PHASE_TOURNAMENT));
        assertThat(meta.getStatus(), is("STARTING"));
    }

    @Test
    @Order(13)
    void get_tournament_ready_to_start_throws_for_non_starting_tournament() {
        assertThrows(IllegalStateException.class,
                () -> TournamentService.getTournamentReadyToStart(ACTIVE_TOURNAMENT));
    }

    @Test
    @Order(14)
    void get_registrations_returns_all_players_for_seating_phase_tournament() {
        List<TournamentRegistration> registrations = TournamentService.getRegistrations(SEATING_PHASE_TOURNAMENT);
        assertThat(registrations, hasSize(8));
    }

    // --- Phase: ACTIVE (play) ---

    @Test
    @Order(15)
    void active_tournament_not_shown_in_open_tournaments_for_players() {
        List<String> names = TournamentService.getOpenTournaments()
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, not(hasItem(ACTIVE_TOURNAMENT)));
    }

    @Test
    @Order(16)
    void active_tournament_not_shown_in_registered_tournaments_view() {
        List<String> names = TournamentService.getRegisteredTournaments("Player1")
                .stream().map(TournamentInviteStatus::getName).toList();
        assertThat(names, not(hasItem(ACTIVE_TOURNAMENT)));
    }

    @Test
    @Order(17)
    void active_tournament_appears_in_admin_list_by_status() {
        List<String> names = TournamentService.getTournamentsWithStatus(List.of(GameStatus.ACTIVE))
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, hasItem(ACTIVE_TOURNAMENT));
    }

    // --- Phase: Finals invites ---

    @Test
    @Order(18)
    void finals_invite_is_empty_for_player_not_in_any_seeding() {
        assertThat(TournamentService.getFinalsInvites("Player9"), is(empty()));
    }

    @Test
    @Order(19)
    void finals_invite_returned_for_player_listed_in_active_tournament_seeding() {
        List<String> names = TournamentService.getFinalsInvites("Player1")
                .stream().map(TournamentMetadata::getName).toList();
        assertThat(names, hasItem(ACTIVE_TOURNAMENT));
    }

    @Test
    @Order(20)
    void finals_invite_metadata_contains_full_seeding_list() {
        TournamentMetadata t = TournamentService.getFinalsInvites("Player2")
                .stream().filter(m -> m.getName().equals(ACTIVE_TOURNAMENT))
                .findFirst().orElseThrow();
        assertThat(t.getFinalsSeeding(),
                containsInAnyOrder("Player1", "Player2", "Player3", "Player4", "Player5"));
    }

    // --- Mutation tests (run last to avoid corrupting state for read tests above) ---

    @Test
    @Order(21)
    void publish_changes_status_from_edit_to_starting() {
        assertThat(TournamentService.getTournamentsWithStatus(List.of(GameStatus.EDIT))
                .stream().map(TournamentMetadata::getName).toList(), hasItem(DRAFT_TOURNAMENT));

        TournamentService.setTournamentStatus(DRAFT_TOURNAMENT, GameStatus.STARTING);

        assertThat(TournamentService.getTournamentsWithStatus(List.of(GameStatus.EDIT))
                .stream().map(TournamentMetadata::getName).toList(), not(hasItem(DRAFT_TOURNAMENT)));
        assertThat(TournamentService.getTournamentsWithStatus(List.of(GameStatus.STARTING))
                .stream().map(TournamentMetadata::getName).toList(), hasItem(DRAFT_TOURNAMENT));
    }

    @Test
    @Order(22)
    void clear_registrations_removes_all_player_registrations() {
        assertThat(TournamentService.getRegistrations(REGISTRATION_OPEN_TOURNAMENT), hasSize(8));

        TournamentService.clearRegistrations(REGISTRATION_OPEN_TOURNAMENT);

        assertThat(TournamentService.getRegistrations(REGISTRATION_OPEN_TOURNAMENT), is(empty()));
    }

    @Test
    @Order(23)
    void clear_registrations_is_idempotent_when_already_empty() {
        TournamentService.clearRegistrations(REGISTRATION_OPEN_TOURNAMENT);
        assertThat(TournamentService.getRegistrations(REGISTRATION_OPEN_TOURNAMENT), is(empty()));
    }

    @Test
    @Order(24)
    void clear_registrations_on_one_tournament_does_not_affect_others() {
        assertThat(TournamentService.getRegistrations(SEATING_PHASE_TOURNAMENT), hasSize(8));
    }
}
