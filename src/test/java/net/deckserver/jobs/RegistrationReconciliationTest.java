package net.deckserver.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;
import net.deckserver.game.enums.Visibility;
import net.deckserver.services.DataPaths;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.RegistrationStatus;
import net.deckserver.storage.json.system.TournamentDefinition;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class RegistrationReconciliationTest {

    /**
     * Game/tournament ids this test class creates ad-hoc via GameService.create/
     * TournamentService.createTournament - directory creation and tournament deck
     * registration have no in-memory cache backstop, so they still write real files
     * even under ENABLE_TEST_MODE. Cleaned up in {@link #cleanUpCreatedEntities()} so
     * they don't linger as untracked files under src/test/resources/data.
     */
    private static final Set<String> createdGameIds = new HashSet<>();
    private static final Set<String> createdTournaments = new HashSet<>();

    @Test
    void purges_registrations_for_a_game_that_no_longer_exists() {
        String gameName = "Deleted Game " + UUID.randomUUID();
        RegistrationService.put(gameName, "Player1", new RegistrationStatus("deck-1"));

        new RegistrationReconciliation().run();

        assertThat(RegistrationService.getGameRegistrations(gameName).isEmpty(), is(true));
    }

    @Test
    void removes_stale_registration_for_player_not_seated_in_active_game() throws IOException {
        String gameName = "Recon Active Game " + UUID.randomUUID();
        String gameId = UUID.randomUUID().toString();
        ExtendedDeck deck = loadTestDeck("deck1.json");

        createdGameIds.add(gameId);
        GameService.create(gameName, gameId, "Player1", Visibility.PUBLIC, GameFormat.STANDARD);
        JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
        jolGame.addPlayer("Player1", deck.getDeck());
        jolGame.addPlayer("Player2", deck.getDeck());
        jolGame.startGame(List.of("Player1", "Player2"));
        GameService.saveGame(jolGame);
        GameService.get(gameName).setStatus(GameStatus.ACTIVE);

        RegistrationService.registerDeck(gameName, "Player1", "deck-1", "Deck One", "summary");
        RegistrationService.registerDeck(gameName, "Player2", "deck-2", "Deck Two", "summary");
        RegistrationService.registerDeck(gameName, "PlayerGhost", "deck-3", "Ghost Deck", "summary");

        new RegistrationReconciliation().run();

        assertThat(RegistrationService.getPlayers(gameName), containsInAnyOrder("Player1", "Player2"));
    }

    @Test
    void non_active_game_is_left_untouched() {
        String gameName = "Recon Starting Game " + UUID.randomUUID();
        String gameId = UUID.randomUUID().toString();
        createdGameIds.add(gameId);
        GameService.create(gameName, gameId, "Player1", Visibility.PUBLIC, GameFormat.STANDARD);
        // GameService.create() leaves the game in STARTING status by default

        RegistrationService.registerDeck(gameName, "PlayerGhost", "deck-2", "Ghost Deck", "summary");

        new RegistrationReconciliation().run();

        assertThat(RegistrationService.getPlayers(gameName), containsInAnyOrder("PlayerGhost"));
    }

    @Test
    void restores_missing_registration_for_seated_tournament_player() throws IOException {
        String tourName = "Reconciliation Tournament " + UUID.randomUUID();
        createdTournaments.add(tourName);
        TournamentDefinition def = new TournamentDefinition();
        def.setName(tourName);
        def.setFormat(TournamentFormat.SINGLE_DECK);
        def.setDeckFormat(GameFormat.STANDARD);
        def.setNumberOfRounds(1);
        def.setStatus(GameStatus.STARTING);
        OffsetDateTime now = OffsetDateTime.now();
        def.setRegistrationStart(now.minusDays(2));
        def.setRegistrationEnd(now.plusDays(1));
        def.setPlayStarts(now.minusHours(1));
        def.setPlayEnds(now.plusDays(1));
        TournamentService.createTournament(def);

        ExtendedDeck deck = loadTestDeck("deck1.json");
        for (String player : List.of("Player1", "Player2")) {
            TournamentService.joinTournament(tourName, player, "00000000");
            TournamentService.registerDeck(tourName, player, deck);
        }
        TournamentService.importRoundsFromCsv(tourName, "Round,Table,Player\n1,1,Player1\n1,1,Player2\n");
        TournamentService.createTournamentTables(tourName);

        String gameName = tourName + ": Round 1 - Table 1";
        RegistrationService.removePlayer(gameName, "Player2");
        assertThat(RegistrationService.getPlayers(gameName), containsInAnyOrder("Player1"));

        new RegistrationReconciliation().run();

        assertThat(RegistrationService.getPlayers(gameName), containsInAnyOrder("Player1", "Player2"));
        assertThat(RegistrationService.isRegistered(gameName, "Player2"), is(true));
    }

    @Test
    void does_not_fabricate_registration_for_non_tournament_game() throws IOException {
        String gameName = "Recon Missing NonTournament " + UUID.randomUUID();
        String gameId = UUID.randomUUID().toString();
        createdGameIds.add(gameId);
        ExtendedDeck deck = loadTestDeck("deck1.json");

        GameService.create(gameName, gameId, "Player1", Visibility.PUBLIC, GameFormat.STANDARD);
        JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
        jolGame.addPlayer("Player1", deck.getDeck());
        jolGame.addPlayer("Player2", deck.getDeck());
        jolGame.startGame(List.of("Player1", "Player2"));
        GameService.saveGame(jolGame);
        GameService.get(gameName).setStatus(GameStatus.ACTIVE);

        RegistrationService.registerDeck(gameName, "Player1", "deck-1", "Deck One", "summary");
        // Player2 is seated but was never registered - no tournament link exists to repair it from

        new RegistrationReconciliation().run();

        assertThat(RegistrationService.getPlayers(gameName), containsInAnyOrder("Player1"));
    }

    private static ExtendedDeck loadTestDeck(String fileName) throws IOException {
        return new ObjectMapper().readValue(Paths.get("src/test/resources/data/decks/" + fileName).toFile(), ExtendedDeck.class);
    }

    @AfterAll
    static void cleanUpCreatedEntities() {
        for (String tourName : createdTournaments) {
            TournamentDefinition def = TournamentService.getTournament(tourName);
            if (def == null) continue;
            for (GameInfo game : GameService.getGamesByTournament(tourName)) {
                deleteDirectoryQuietly(DataPaths.path("games", game.getId()));
            }
            deleteDirectoryQuietly(DataPaths.path("tournaments", def.getId()));
        }
        for (String gameId : createdGameIds) {
            deleteDirectoryQuietly(DataPaths.path("games", gameId));
        }
    }

    private static void deleteDirectoryQuietly(Path path) {
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException ignored) {
            // best-effort test cleanup
        }
    }
}
