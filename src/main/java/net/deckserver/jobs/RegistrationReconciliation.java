package net.deckserver.jobs;

import net.deckserver.game.model.JolGame;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.services.TournamentService;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.TournamentRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Reconciles registrations.json against the actual game state: removes registration rows
 * that no longer correspond to a real, currently-seated player and (for tournament games,
 * where the deck link is recoverable via TournamentService) restores rows that are missing.
 * <p>
 * Runs once at server startup. Registration rows can drift out of sync with games.json when a
 * game is deleted/recreated (e.g. the tournament "recreate table" admin action) without every
 * downstream registration row being cleaned up - a stale row pointing at a deleted game breaks
 * login, since MainBean/PlayerResource resolve registered games via GameService.isActive(...),
 * which is not null-safe.
 */
public class RegistrationReconciliation implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationReconciliation.class);

    @Override
    public void run() {
        for (String gameName : RegistrationService.getRegisteredGameNames()) {
            GameInfo info = GameService.get(gameName);
            if (info == null) {
                purgeOrphanedGame(gameName);
            } else if (info.getStatus() == GameStatus.ACTIVE) {
                reconcileActiveGame(gameName, info);
            }
            // Games that exist but aren't ACTIVE (STARTING/EDIT/CLOSED) are out of scope.
        }
    }

    private void purgeOrphanedGame(String gameName) {
        Map<String, ?> rows = RegistrationService.getGameRegistrations(gameName);
        if (rows.isEmpty()) return;
        logger.info("Purging {} orphaned registration(s) for deleted game '{}'", rows.size(), gameName);
        RegistrationService.clearRegistrations(gameName);
    }

    private void reconcileActiveGame(String gameName, GameInfo info) {
        JolGame game = GameService.getGame(info.getId());
        Set<String> actual = new HashSet<>(game.getPlayers());
        Set<String> registered = new HashSet<>(RegistrationService.getPlayers(gameName));

        for (String player : registered) {
            if (!actual.contains(player)) {
                logger.info("Removing stale registration for '{}' in active game '{}' (not seated in game)", player, gameName);
                RegistrationService.removePlayer(gameName, player);
            }
        }

        String tournamentName = info.getTournamentName();
        for (String player : actual) {
            if (registered.contains(player)) continue;
            restoreMissingRegistration(gameName, tournamentName, player);
        }
    }

    private void restoreMissingRegistration(String gameName, String tournamentName, String player) {
        if (tournamentName == null || tournamentName.isEmpty()) {
            logger.warn("Player '{}' is seated in active game '{}' with no registration and no tournament link - " +
                    "cannot safely reconstruct deck info, skipping", player, gameName);
            return;
        }

        Optional<TournamentRegistration> registration = TournamentService.getRegistrations(tournamentName, player);
        if (registration.isEmpty() || registration.get().getDeck() == null) {
            logger.warn("Player '{}' is seated in active tournament game '{}' but has no tournament deck registration - skipping", player, gameName);
            return;
        }

        String deckId = registration.get().getDeck();
        ExtendedDeck deck = TournamentService.getTournamentDeck(tournamentName, deckId);
        if (deck == null || deck.getDeck() == null || deck.getStats() == null || deck.getStats().getSummary() == null) {
            logger.warn("Unable to load tournament deck '{}' for player '{}' in game '{}' - skipping registration repair", deckId, player, gameName);
            return;
        }

        logger.info("Restoring missing registration for '{}' in active game '{}'", player, gameName);
        RegistrationService.registerDeck(gameName, player, deckId, deck.getDeck().getName(), deck.getStats().getSummary());
    }
}
