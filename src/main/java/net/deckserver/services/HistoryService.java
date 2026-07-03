package net.deckserver.services;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameHistoryRepository;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryService extends PersistedService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private static final GameHistoryRepository gameHistoryRepository = new GameHistoryRepository();
    private static final HistoryService INSTANCE = new HistoryService();

    private final Map<OffsetDateTime, GameHistory> pastGames = new ConcurrentHashMap<>();

    private HistoryService() {
        super("HistoryService", 0);
        load();
    }

    public static  Map<OffsetDateTime, GameHistory> getHistory() {
        return INSTANCE.pastGames;
    }

    public static  void addGame(OffsetDateTime now, GameHistory history) {
        INSTANCE.pastGames.put(now, history);
        INSTANCE.jpaWrite(em -> gameHistoryRepository.save(em, now, history));
    }

    public static  Collection<GameHistory> getGames() {
        return INSTANCE.pastGames.values();
    }

    public static  void validateGW() {
        INSTANCE.pastGames.forEach((recordedAt, gameHistory) -> {
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
                INSTANCE.jpaWrite(em -> gameHistoryRepository.save(em, recordedAt, gameHistory));
            }
        });
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            pastGames.putAll(gameHistoryRepository.findAll(em));
            logger.info("Loaded {} game histories from JPA", pastGames.size());
        } catch (Exception e) {
            logger.error("JPA load failed for HistoryService", e);
        }
    }
}
