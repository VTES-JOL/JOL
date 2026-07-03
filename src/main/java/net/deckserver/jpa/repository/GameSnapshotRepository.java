package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameSnapshotEntity;
import net.deckserver.jpa.entity.GameSnapshotId;
import net.deckserver.storage.json.game.GameData;

import java.time.OffsetDateTime;

public class GameSnapshotRepository {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(EntityManager em, String gameId, String turn, GameData data) {
        String state;
        try {
            state = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize snapshot for game " + gameId + " turn " + turn, e);
        }
        GameSnapshotId id = new GameSnapshotId(gameId, turn);
        GameSnapshotEntity existing = em.find(GameSnapshotEntity.class, id);
        if (existing != null) {
            existing.setState(state);
            existing.setCreatedAt(OffsetDateTime.now());
        } else {
            em.persist(new GameSnapshotEntity(id, state, OffsetDateTime.now()));
        }
    }

    /**
     * @return the snapshot state, or null when no snapshot exists for that turn
     */
    public GameData load(EntityManager em, String gameId, String turn) {
        GameSnapshotEntity entity = em.find(GameSnapshotEntity.class, new GameSnapshotId(gameId, turn));
        if (entity == null) return null;
        try {
            return mapper.readValue(entity.getState(), GameData.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize snapshot for game " + gameId + " turn " + turn, e);
        }
    }

    public void deleteAllForGame(EntityManager em, String gameId) {
        em.createQuery("DELETE FROM GameSnapshotEntity s WHERE s.id.gameId = :gameId")
                .setParameter("gameId", gameId)
                .executeUpdate();
    }
}
