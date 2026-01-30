package net.deckserver.jobs;

import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.services.*;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.TournamentMetadata;
import net.deckserver.storage.json.system.TournamentPlayer;
import net.deckserver.storage.json.system.TournamentRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public class TournamentJob implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TournamentJob.class);

    @Override
    public void run() {
        // Start tournaments
        List<TournamentMetadata> tournaments = TournamentService.getTournamentsReadyToStart();
        for (TournamentMetadata tournament : tournaments) {
            String tournamentName = tournament.getName();
            log.info("Starting tournament {}", tournamentName);
            for (int round = 1; round <= tournament.getNumberOfRounds(); round++) {
                for (int table = 1; table <= tournament.getNumberOfTables(); table++) {
                    String gameName = String.format("%s: Round %d - Table %d", tournamentName, round, table);
                    String gameId = UUID.randomUUID().toString();
                    // Create Game
                    GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.from(tournament.getDeckFormat()));
                    JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
                    List<TournamentPlayer> players = TournamentService.getPlayers(tournamentName, round, table);
                    for (TournamentPlayer player : players) {
                        String playerName = player.getName();
                        TournamentRegistration registration = TournamentService.getRegistrations(tournamentName, playerName).orElseThrow();
                        String deckId = registration.getDeck();
                        ExtendedDeck deck = TournamentService.getTournamentDeck(tournamentName, deckId);
                        assert deck != null;
                        // Create Registration
                        RegistrationService.registerDeck(gameName, playerName, deckId, deck.getDeck().getName(), deck.getStats().getSummary());
                        // Add player and deck
                        jolGame.addPlayer(playerName, deck.getDeck());
                    }
                    // Set order and start
                    jolGame.startGame(players.stream().map(TournamentPlayer::getName).toList());
                    // Save game
                    GameService.saveGame(jolGame);
                    // Update status
                    GameService.get(gameName).setStatus(GameStatus.ACTIVE);
                }
            }
            // Start tournament
            TournamentService.startTournament(tournamentName);
        }

        List<TournamentMetadata> runningTournaments = TournamentService.getActiveTournaments();

        // Create final tables
        for (TournamentMetadata tournament : runningTournaments) {
            String tournamentName = tournament.getName();
            List<String> seeding = tournament.getFinalsSeeding();
            String gameName = String.format("%s: Final Table", tournamentName);
            GameInfo gameInfo = GameService.get(gameName);
            if (gameInfo == null && !seeding.isEmpty()) {
                String gameId = UUID.randomUUID().toString();
                GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.from(tournament.getDeckFormat()));
                JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
                for (String playerName : seeding) {
                    String deckId = TournamentService.getRegistrations(tournamentName, playerName).map(TournamentRegistration::getDeck).orElseThrow();
                    ExtendedDeck deck = TournamentService.getTournamentDeck(tournamentName, deckId);
                    assert deck != null;
                    // Create Registration
                    RegistrationService.registerDeck(gameName, playerName, deckId, deck.getDeck().getName(), deck.getStats().getSummary());
                    // Add player and deck
                    jolGame.addPlayer(playerName, deck.getDeck());
                    NotificationService.pingPlayer(playerName, null, gameName);
                }
                // Set order and start
                jolGame.startGame(seeding);
                // Save game
                GameService.saveGame(jolGame);
                // Update status
                GameService.get(gameName).setStatus(GameStatus.ACTIVE);
                GlobalChatService.chat("SYSTEM", String.format("Game %s started", gameName));
            }
        }

        // Check running tournaments have decks
        for (TournamentMetadata tournament : runningTournaments) {
            String tournamentName = tournament.getName();
            for (int round = 1; round <= tournament.getNumberOfRounds(); round++) {
                for (int table = 1; table <= tournament.getNumberOfTables(); table++) {
                    String gameName = String.format("%s: Round %d - Table %d", tournamentName, round, table);
                    String gameId = GameService.get(gameName).getId();
                    List<TournamentPlayer> players = TournamentService.getPlayers(tournamentName, round, table);
                    for (TournamentPlayer player : players) {
                        String playerName = player.getName();
                        var registration = TournamentService.getRegistrations(tournamentName, playerName).orElseThrow();
                        Path gameDeckPath = Paths.get(System.getenv("JOL_DATA"), "games", gameId, registration.getDeck() + ".json");
                        Path tournamentDeckPath = Paths.get(System.getenv("JOL_DATA"), "tournaments", tournament.getId(), registration.getDeck() + ".json");
                        if (!Files.exists(gameDeckPath)) {
                            try {
                                Files.copy(tournamentDeckPath, gameDeckPath, StandardCopyOption.REPLACE_EXISTING);
                                log.info("Copying missing tournament game file for {} - {} Round {} - Table {}", tournamentName, playerName, round, table);
                            } catch (IOException e) {
                                log.error("Unable to copy tournament file");
                            }
                        }
                    }
                }
            }
        }
    }
}
