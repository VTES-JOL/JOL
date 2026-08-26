package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameHistoryEntity;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameHistoryRepository {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Map<OffsetDateTime, GameHistory> findAll(EntityManager em) {
        Map<OffsetDateTime, GameHistory> histories = new LinkedHashMap<>();
        em.createQuery("SELECT h FROM GameHistoryEntity h ORDER BY h.recordedAt", GameHistoryEntity.class)
                .getResultStream()
                .forEach(entity -> histories.put(entity.getRecordedAt(), toGameHistory(entity)));
        return histories;
    }

    public void save(EntityManager em, OffsetDateTime recordedAt, GameHistory history) {
        GameHistoryEntity entity = em.createQuery(
                        "SELECT h FROM GameHistoryEntity h WHERE h.recordedAt = :recordedAt", GameHistoryEntity.class)
                .setParameter("recordedAt", recordedAt)
                .getResultStream()
                .findFirst()
                .orElse(null);
        boolean isNew = entity == null;
        if (isNew) {
            entity = new GameHistoryEntity();
            entity.setRecordedAt(recordedAt);
        }
        entity.setGameName(history.getName());
        entity.setStarted(history.getStarted());
        entity.setEnded(history.getEnded());
        try {
            entity.setResults(mapper.writeValueAsString(history.getResults()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize results for game history " + history.getName(), e);
        }
        if (isNew) {
            em.persist(entity);
        }
    }

    public List<GameHistoryEntity> findAllEntities(EntityManager em) {
        return em.createQuery("SELECT h FROM GameHistoryEntity h ORDER BY h.recordedAt", GameHistoryEntity.class)
                .getResultList();
    }

    public void update(EntityManager em, GameHistoryEntity entity) {
        em.merge(entity);
    }

    private GameHistory toGameHistory(GameHistoryEntity entity) {
        GameHistory history = new GameHistory();
        history.setName(entity.getGameName());
        history.setStarted(entity.getStarted());
        history.setEnded(entity.getEnded());
        try {
            List<PlayerResult> results = mapper.readValue(entity.getResults(), new TypeReference<>() {});
            history.setResults(results);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize results for game history " + entity.getGameName(), e);
        }
        return history;
    }
}
