package net.deckserver.jobs;

import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.TournamentMetadata;
import net.deckserver.storage.json.system.TournamentPlayer;
import net.deckserver.storage.json.system.TournamentRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class TournamentJob implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TournamentJob.class);

    @Override
    public void run() {
        List<TournamentMetadata> tournaments = TournamentService.getTournamentsReadyToStart();
        for (TournamentMetadata tournament : tournaments) {
            String tournamentName = tournament.getName();
            log.info("Starting tournament " + tournamentName);
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
    }
}
