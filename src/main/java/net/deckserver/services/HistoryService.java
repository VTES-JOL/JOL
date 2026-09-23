package net.deckserver.services;

import com.fasterxml.jackson.databind.type.TypeFactory;
import net.deckserver.game.GameOutcome;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryService extends PersistedService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private static final Path PERSISTENCE_PATH = DataPaths.path("pastGames.json");
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

    /**
     * Sweeps every stored game: drops records that can never be valid and rewrites the game-win
     * flags on the rest so they match {@link GameOutcome}. Safe to run repeatedly - it is invoked
     * on load and can also be scheduled as a maintenance job.
     */
    public static void validateGW() {
        INSTANCE.validate();
    }

    private void validate() {
        int before = pastGames.size();
        pastGames.entrySet().removeIf(entry -> {
            String reason = invalidReason(entry.getValue());
            if (reason != null) {
                logger.warn("Deleting invalid game history '{}' ({}): {}", entry.getValue().getName(), entry.getKey(), reason);
                return true;
            }
            return false;
        });
        int removed = before - pastGames.size();
        if (removed > 0) {
            logger.warn("Removed {} invalid game history record(s)", removed);
        }
        pastGames.values().forEach(HistoryService::reconcileGameWin);
    }

    /** @return a human-readable reason the record is unusable, or {@code null} if it is structurally sound */
    static String invalidReason(GameHistory game) {
        List<PlayerResult> results = game == null ? null : game.getResults();
        if (results == null || results.isEmpty()) {
            return "no player results";
        }
        if (results.stream().anyMatch(r -> r.getPlayerName() == null || r.getPlayerName().isBlank())) {
            return "a result is missing its player name";
        }
        if (results.stream().anyMatch(r -> r.getVictoryPoints() == null)) {
            return "a result is missing its victory points";
        }
        double totalVp = results.stream().mapToDouble(PlayerResult::getVictoryPoints).sum();
        if (!GameOutcome.isPlausibleVictoryPointTotal(results.size(), totalVp)) {
            return String.format("recorded VP total %s is invalid for a %d player game", totalVp, results.size());
        }
        return null;
    }

    /** Rewrites {@code gameWin} on every result of a (already validated) game to match {@link GameOutcome}. */
    static void reconcileGameWin(GameHistory game) {
        PlayerResult winner = GameOutcome.gameWinner(game.getResults(), PlayerResult::getVictoryPoints).orElse(null);
        for (PlayerResult result : game.getResults()) {
            boolean shouldWin = result == winner;
            if (result.isGameWin() != shouldWin) {
                logger.info("{} - {} game win for {} on {} VP", game.getName(),
                        shouldWin ? "recording" : "clearing stale", result.getPlayerName(), result.getVictoryPoints());
                result.setGameWin(shouldWin);
            }
        }
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
            validate();
        } catch (IOException e) {
            logger.error("Unable to load game history.", e);
        }
    }
}
