package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import com.fasterxml.jackson.core.type.TypeReference;
import net.deckserver.jpa.entity.GameHistoryEntity;
import net.deckserver.jpa.repository.GameHistoryRepository;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;

@Singleton
@Startup
public class HistoryService extends PersistedService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private static final GameHistoryRepository gameHistoryRepository = new GameHistoryRepository();
    private static HistoryService instance() {
        return resolve(HistoryService.class, HistoryService::new);
    }

    HistoryService() {
        super("HistoryService", 0);
    }

    public static Map<OffsetDateTime, GameHistory> getHistory() {
        Map<OffsetDateTime, GameHistory> result = instance().jpaRead(gameHistoryRepository::findAll);
        return result != null ? result : Map.of();
    }

    public static void addGame(OffsetDateTime now, GameHistory history) {
        instance().requireJpaWriteAlways(em -> gameHistoryRepository.save(em, now, history));
    }

    public static Collection<GameHistory> getGames() {
        return getHistory().values();
    }

    public static void validateGW() {
        Collection<GameHistoryEntity> entities = instance().jpaRead(gameHistoryRepository::findAllEntities);
        if (entities == null) return;
        entities.forEach(entity -> {
            GameHistory gameHistory;
            try {
                gameHistory = new GameHistory();
                gameHistory.setName(entity.getGameName());
                gameHistory.setStarted(entity.getStarted());
                gameHistory.setEnded(entity.getEnded());
                gameHistory.setResults(objectMapper.readValue(entity.getResults(), new TypeReference<>() {}));
            } catch (Exception e) {
                logger.error("Failed to deserialise game history for validation: {}", entity.getGameName(), e);
                return;
            }

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
            boolean changed = false;
            if (winner != null && previousWinner == null) {
                logger.info("Found a winner for {} where there wasn't one before, now {} on {}", gameHistory.getName(), winner.getPlayerName(), winner.getVictoryPoints());
                winner.setGameWin(true);
                changed = true;
            } else if (winner != null && winner != previousWinner) {
                logger.info("Found a new winner for {}, previously {} on {}, now {} on {}", gameHistory.getName(), previousWinner.getPlayerName(), previousWinner.getVictoryPoints(), winner.getPlayerName(), winner.getVictoryPoints());
                winner.setGameWin(true);
                previousWinner.setGameWin(false);
                changed = true;
            }
            if (changed) {
                try {
                    entity.setResults(objectMapper.writeValueAsString(gameHistory.getResults()));
                    instance().requireJpaWriteAlways(em -> gameHistoryRepository.update(em, entity));
                } catch (Exception e) {
                    logger.error("Failed to serialise updated results for {}", gameHistory.getName(), e);
                }
            }
        });
    }

    public static PersistedService getInstance() {
        return instance();
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
    }

    @Override
    protected void load() {
        // no startup load needed — reads go directly to JPA
    }
}
