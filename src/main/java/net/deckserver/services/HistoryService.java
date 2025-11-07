package net.deckserver.services;

import com.fasterxml.jackson.databind.type.TypeFactory;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryService extends PersistedService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "pastGames.json");
    private static final HistoryService INSTANCE = new HistoryService();

    private final Map<OffsetDateTime, GameHistory> pastGames = new HashMap<>();

    private HistoryService() {
        super("HistoryService", 10);
        load();
    }

    public static  Map<OffsetDateTime, GameHistory> getHistory() {
        return INSTANCE.pastGames;
    }

    public static  void addGame(OffsetDateTime now, GameHistory history) {
        INSTANCE.pastGames.put(now, history);
    }

    public static  Collection<GameHistory> getGames() {
        return INSTANCE.pastGames.values();
    }

    public static  void validateGW() {
        HistoryService.getGames().forEach(gameHistory -> {
            PlayerResult winner = null;
            PlayerResult previousWinner = gameHistory.getResults().stream().filter(PlayerResult::isGameWin).findFirst().orElse(null);
            double topVP = 0.0;
            for (PlayerResult result : gameHistory.getResults()) {
                double victoryPoints = result.getVictoryPoints();
                if (victoryPoints >= 2.0) {
                    if (winner == null) {
                        logger.debug("{} - {} has {} VP and there is no current high score.", gameHistory.getName(), result.getPlayerName(), victoryPoints);
                        winner = result;
                        topVP = victoryPoints;
                    } else if (victoryPoints > topVP) {
                        logger.debug("{} - {} has {} VP, previous high score was {} on {} VP.", gameHistory.getName(), result.getPlayerName(), victoryPoints, winner.getPlayerName(), topVP);
                        winner = result;
                        topVP = victoryPoints;
                    } else if (victoryPoints == topVP) {
                        logger.debug("{} - tie between {} and {}. No winner.", gameHistory.getName(), result.getPlayerName(), winner.getPlayerName());
                        winner = null;
                    }
                }
            }
            if (winner != null && previousWinner == null) {
                logger.info("Found a winner for {} where there wasn't one before, now {} on {}", gameHistory.getName(), winner.getPlayerName(), winner.getVictoryPoints());
                winner.setGameWin(true);
            } else if (winner != null && winner != previousWinner) {
                logger.info("Found a new winner for {}, previously {} on {}, now {} on {}", gameHistory.getName(), previousWinner.getPlayerName(), previousWinner.getVictoryPoints(), winner.getPlayerName(), winner.getVictoryPoints());
                winner.setGameWin(true);
                previousWinner.setGameWin(false);
            }
        });
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} past games", pastGames.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), pastGames);
            logger.debug("Successfully persisted past games");
        } catch (IOException e) {
            logger.error("Unable to save past games", e);
        }

    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing game histories file found");
            return;
        }

        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            Map<OffsetDateTime, GameHistory> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), typeFactory.constructMapType(ConcurrentHashMap.class, OffsetDateTime.class, GameHistory.class));
            pastGames.putAll(loaded);
            logger.info("Loaded {} game histories", loaded.size());
        } catch (IOException e) {
            logger.error("Unable to load game history.", e);
        }
    }
}
