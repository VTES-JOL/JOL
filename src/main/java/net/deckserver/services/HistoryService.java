package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import com.fasterxml.jackson.core.type.TypeReference;
import net.deckserver.game.GameOutcome;
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

            PlayerResult winner = GameOutcome.gameWinner(gameHistory.getResults(),
                    r -> r.getVictoryPoints() == null ? 0.0 : r.getVictoryPoints()).orElse(null);
            boolean changed = false;
            for (PlayerResult result : gameHistory.getResults()) {
                boolean shouldWin = result == winner;
                if (result.isGameWin() != shouldWin) {
                    logger.info("{} - {} game win for {} on {} VP", gameHistory.getName(),
                            shouldWin ? "recording" : "clearing stale", result.getPlayerName(), result.getVictoryPoints());
                    result.setGameWin(shouldWin);
                    changed = true;
                }
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
