package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;
import net.deckserver.game.enums.Visibility;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.TournamentDefinition;
import net.deckserver.storage.json.system.TournamentInviteStatus;
import net.deckserver.storage.json.system.TournamentMetadata;
import net.deckserver.storage.json.system.TournamentPlayer;
import net.deckserver.storage.json.system.TournamentRegistration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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

    // --- Round CSV import ---

    @Test
    @Order(25)
    void import_rounds_from_csv_parses_well_formed_data() throws IOException {
        String csv = """
                Round,Table,Player
                1,1,Player1
                1,1,Player2
                """;

        TournamentService.importRoundsFromCsv(SEATING_PHASE_TOURNAMENT, csv);

        List<TournamentPlayer> players = TournamentService.getPlayers(SEATING_PHASE_TOURNAMENT, 1, 1);
        assertThat(players.stream().map(TournamentPlayer::getName).toList(),
                containsInAnyOrder("Player1", "Player2"));
    }

    @Test
    @Order(26)
    void import_rounds_from_csv_tolerates_bom_and_curly_quotes() throws IOException {
        String csv = "﻿Round,Table,Player\n” 7 ”,”2”,Player3\n";

        TournamentService.importRoundsFromCsv(SEATING_PHASE_TOURNAMENT, csv);

        List<TournamentPlayer> players = TournamentService.getPlayers(SEATING_PHASE_TOURNAMENT, 7, 2);
        assertThat(players.stream().map(TournamentPlayer::getName).toList(), hasItem("Player3"));
    }

    @Test
    @Order(27)
    void import_rounds_from_csv_throws_helpful_error_and_leaves_state_untouched() {
        String csv = "Round,Table,Player\n1,1,Player1\nabc,2,Player2\n";

        Map<Integer, Map<Integer, List<TournamentPlayer>>> before = TournamentService.getTournament(SEATING_PHASE_TOURNAMENT).getRounds();

        IOException exception = assertThrows(IOException.class,
                () -> TournamentService.importRoundsFromCsv(SEATING_PHASE_TOURNAMENT, csv));

        assertThat(exception.getMessage(), allOf(
                containsString("record 2"),
                containsString("'Round'"),
                containsString("abc")));
        assertThat(TournamentService.getTournament(SEATING_PHASE_TOURNAMENT).getRounds(), is(before));
    }

    @Test
    @Order(28)
    void import_rounds_from_csv_clears_stale_games_for_starting_tournament() throws IOException {
        String gameName = SEATING_PHASE_TOURNAMENT + ": Round 1 - Table 1";
        String gameId = UUID.randomUUID().toString();
        GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.STANDARD);
        GameService.get(gameName).setTournamentName(SEATING_PHASE_TOURNAMENT);
        assertThat(GameService.getGamesByTournament(SEATING_PHASE_TOURNAMENT), hasSize(1));

        TournamentDefinition tournament = TournamentService.getTournament(SEATING_PHASE_TOURNAMENT);
        assertThat(tournament.getStatus(), is(GameStatus.STARTING));

        TournamentService.importRoundsFromCsv(SEATING_PHASE_TOURNAMENT, "Round,Table,Player\n1,1,Player1\n");

        assertThat(GameService.getGamesByTournament(SEATING_PHASE_TOURNAMENT), is(empty()));
    }

    @Test
    @Order(29)
    void create_tournament_tables_fails_fast_and_names_player_missing_registration() throws IOException {
        TournamentService.importRoundsFromCsv(SEATING_PHASE_TOURNAMENT, "Round,Table,Player\n1,1,PlayerGhost\n");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> TournamentService.createTournamentTables(SEATING_PHASE_TOURNAMENT));

        assertThat(exception.getMessage(), allOf(
                containsString("PlayerGhost"),
                containsString("no registration found")));
        assertThat(GameService.existsGame(SEATING_PHASE_TOURNAMENT + ": Round 1 - Table 1"), is(false));
    }

    @Test
    @Order(30)
    void create_tournament_tables_reports_single_deck_issue_once_across_rounds() throws IOException {
        String tourName = "Single Deck Validation Test";
        TournamentService.createTournament(newTournament(tourName, TournamentFormat.SINGLE_DECK));
        TournamentService.importRoundsFromCsv(tourName, "Round,Table,Player\n1,1,PlayerGhost\n2,1,PlayerGhost\n");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> TournamentService.createTournamentTables(tourName));

        assertThat(occurrences(exception.getMessage(), "PlayerGhost"), is(1));
        assertThat(exception.getMessage(), not(containsString("Round ")));
    }

    @Test
    @Order(31)
    void create_tournament_tables_reports_multi_deck_issue_per_round() throws IOException {
        String tourName = "Multi Deck Validation Test";
        TournamentService.createTournament(newTournament(tourName, TournamentFormat.MULTI_DECK));
        TournamentService.importRoundsFromCsv(tourName, "Round,Table,Player\n1,1,PlayerGhost\n2,1,PlayerGhost\n");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> TournamentService.createTournamentTables(tourName));

        assertThat(occurrences(exception.getMessage(), "PlayerGhost"), is(2));
        assertThat(exception.getMessage(), allOf(containsString("Round 1:"), containsString("Round 2:")));
    }

    // --- Recreate a single round/table on an ACTIVE tournament ---

    @Test
    @Order(32)
    void recreate_table_requires_active_tournament() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> TournamentService.recreateTable(SEATING_PHASE_TOURNAMENT, 1, 1, "Round,Table,Player\n1,1,Player1\n"));

        assertThat(exception.getMessage(), containsString("not ACTIVE"));
    }

    @Test
    @Order(33)
    void recreate_table_rejects_csv_rows_for_a_different_table() throws IOException {
        String tourName = "Recreate Table Mismatch";
        seatActiveTournamentWithTwoTables(tourName);
        String gameName = tourName + ": Round 1 - Table 1";

        IOException exception = assertThrows(IOException.class,
                () -> TournamentService.recreateTable(tourName, 1, 1, "Round,Table,Player\n1,2,PlayerX\n"));

        assertThat(exception.getMessage(), allOf(containsString("Table 2"), containsString("Table 1")));
        assertThat(GameService.existsGame(gameName), is(true));
    }

    @Test
    @Order(34)
    void recreate_table_rejects_player_already_seated_at_another_table_in_same_round() throws IOException {
        String tourName = "Recreate Table Conflict";
        seatActiveTournamentWithTwoTables(tourName);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> TournamentService.recreateTable(tourName, 1, 1, "Round,Table,Player\n1,1,Player5\n1,1,Player2\n"));

        assertThat(exception.getMessage(), allOf(containsString("Player5"), containsString("table 2")));
        assertThat(TournamentService.getPlayers(tourName, 1, 1).stream().map(TournamentPlayer::getName).toList(),
                containsInAnyOrder("Player1", "Player2", "Player3", "Player4"));
    }

    @Test
    @Order(35)
    void recreate_table_fails_fast_for_missing_registration_and_leaves_state_untouched() throws IOException {
        String tourName = "Recreate Table Deck Issue";
        seatActiveTournamentWithTwoTables(tourName);
        String gameName = tourName + ": Round 1 - Table 1";

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> TournamentService.recreateTable(tourName, 1, 1, "Round,Table,Player\n1,1,PlayerGhost\n"));

        assertThat(exception.getMessage(), allOf(containsString("PlayerGhost"), containsString("no registration found")));
        assertThat(GameService.existsGame(gameName), is(true));
        assertThat(TournamentService.getPlayers(tourName, 1, 1).stream().map(TournamentPlayer::getName).toList(),
                containsInAnyOrder("Player1", "Player2", "Player3", "Player4"));
    }

    @Test
    @Order(36)
    void recreate_table_replaces_game_and_seating_leaving_other_tables_untouched() throws IOException {
        String tourName = "Recreate Table Happy Path";
        seatActiveTournamentWithTwoTables(tourName);
        String gameName = tourName + ": Round 1 - Table 1";
        GameInfo before = GameService.get(gameName);

        TournamentService.recreateTable(tourName, 1, 1, "Round,Table,Player\n1,1,Player1\n1,1,Player3\n");

        GameInfo after = GameService.get(gameName);
        assertThat(after, is(notNullValue()));
        assertThat(after.getId(), not(is(before.getId())));
        assertThat(TournamentService.getPlayers(tourName, 1, 1).stream().map(TournamentPlayer::getName).toList(),
                containsInAnyOrder("Player1", "Player3"));
        assertThat(TournamentService.getPlayers(tourName, 1, 2).stream().map(TournamentPlayer::getName).toList(),
                containsInAnyOrder("Player5", "Player6"));
        assertThat(GameService.existsGame(tourName + ": Round 1 - Table 2"), is(true));
    }

    /** Builds an ACTIVE tournament with two real tables (Round 1: Table 1 = Players 1-4, Table 2 = Players 5-6). */
    private static void seatActiveTournamentWithTwoTables(String tourName) throws IOException {
        TournamentDefinition def = newTournament(tourName, TournamentFormat.SINGLE_DECK);
        def.setRegistrationStart(OffsetDateTime.now().minusDays(2));
        def.setRegistrationEnd(OffsetDateTime.now().plusDays(1));
        TournamentService.createTournament(def);

        ExtendedDeck deck = loadTestDeck("deck1.json");
        for (String player : List.of("Player1", "Player2", "Player3", "Player4", "Player5", "Player6")) {
            TournamentService.joinTournament(tourName, player, "00000000");
            TournamentService.registerDeck(tourName, player, deck);
        }

        TournamentService.importRoundsFromCsv(tourName, """
                Round,Table,Player
                1,1,Player1
                1,1,Player2
                1,1,Player3
                1,1,Player4
                1,2,Player5
                1,2,Player6
                """);
        TournamentService.createTournamentTables(tourName);
    }

    private static ExtendedDeck loadTestDeck(String fileName) throws IOException {
        return new ObjectMapper().readValue(Paths.get("src/test/resources/data/decks/" + fileName).toFile(), ExtendedDeck.class);
    }

    private static TournamentDefinition newTournament(String name, TournamentFormat format) {
        TournamentDefinition def = new TournamentDefinition();
        def.setName(name);
        def.setFormat(format);
        def.setDeckFormat(GameFormat.STANDARD);
        def.setNumberOfRounds(2);
        def.setStatus(GameStatus.STARTING);
        OffsetDateTime now = OffsetDateTime.now();
        def.setRegistrationStart(now.minusDays(2));
        def.setRegistrationEnd(now.minusDays(1));
        def.setPlayStarts(now.minusHours(1));
        def.setPlayEnds(now.plusDays(1));
        return def;
    }

    private static int occurrences(String text, String needle) {
        return (int) Pattern.compile(Pattern.quote(needle)).matcher(text).results().count();
    }
}
