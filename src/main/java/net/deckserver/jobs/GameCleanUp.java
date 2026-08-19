package net.deckserver.jobs;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.services.GameService;
import net.deckserver.services.GlobalChatService;
import net.deckserver.services.HistoryService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.game.GameSummary;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.PlayerResult;
import net.deckserver.storage.json.system.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class GameCleanUp implements Runnable {

    private static final int STALE_LOBBY_DAYS = 5;

    public static final Logger logger = LoggerFactory.getLogger(GameCleanUp.class);

    @Override
    public void run() {
        Table<String, String, Boolean> invalidRegistrations = HashBasedTable.create();
        // Find invites for games that have already started
        GameService.getActiveGames()
                .forEach(gameName -> {
                    Map<String, RegistrationStatus> playerRegistrations = RegistrationService.getGameRegistrations(gameName);
                    playerRegistrations.forEach((player, registration) -> {
                        if (registration.getDeckId() == null) {
                            logger.info("Removing unregistered player {} from active game {}", player, gameName);
                            invalidRegistrations.put(gameName, player, Boolean.TRUE);
                        }
                    });
                });

        // Close finished games (skip tournament games — their lifecycle is managed by tournament admin)
        GameService.getActiveGames()
                .forEach(gameName -> {
                    GameInfo info = GameService.get(gameName);
                    JolGame game = GameService.getGame(info.getId());
                    if (game.getValidPlayers().isEmpty()) {
                        if (info.getTournamentName() != null && !info.getTournamentName().isEmpty()) {
                            logger.info("Skipping cleanup of finished tournament game {}", gameName);
                            return;
                        }
                        logger.info("Closing finished game {}", gameName);
                        JolAdmin.endGame(gameName, true);
                        GlobalChatService.chat("SYSTEM", String.format("%s has been closed.", gameName));
                    }
                });

        // Remove invalid players from games
        invalidRegistrations.cellSet().forEach(registration -> {
            RegistrationService.removePlayer(registration.getRowKey(), registration.getColumnKey());
        });

        // Remove public lobby games that have been idle for too long (deck registration extends the window)
        OffsetDateTime staleCutoff = OffsetDateTime.now().minusDays(STALE_LOBBY_DAYS);
        List<GameInfo> staleLobbyGames = GameService.getGameNames().stream()
                .map(GameService::get)
                .filter(GameService.STARTING_GAME)
                .filter(GameService.PUBLIC_GAME)
                .filter(info -> info.getUpdated() != null && info.getUpdated().isBefore(staleCutoff))
                .toList();

        staleLobbyGames.forEach(info -> {
            logger.info("Removing stale lobby game {}", info.getName());
            GameService.remove(info.getName(), info.getId());
            RegistrationService.clearRegistrations(info.getName());
        });
    }
}
