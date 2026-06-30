package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameChatEntity;
import net.deckserver.storage.json.game.TurnData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public class GameChatRepository {

    private static final Logger logger = LoggerFactory.getLogger(GameChatRepository.class);
    private static final ObjectMapper mapper;
    private static final TypeReference<List<TurnData>> TURN_LIST = new TypeReference<>() {};

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(EntityManager em, String gameId, Collection<TurnData> turns) {
        String history;
        try {
            history = mapper.writeValueAsString(turns);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize TurnHistory for game {}", gameId, e);
            return;
        }

        GameChatEntity existing = em.find(GameChatEntity.class, gameId);
        GameChatEntity entity = existing != null ? existing : new GameChatEntity();
        entity.setGameId(gameId);
        entity.setHistory(history);
        entity.setUpdatedAt(OffsetDateTime.now());

        if (existing != null) {
            em.merge(entity);
        } else {
            em.persist(entity);
        }
    }

    public void delete(EntityManager em, String gameId) {
        GameChatEntity entity = em.find(GameChatEntity.class, gameId);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public List<TurnData> load(EntityManager em, String gameId) {
        GameChatEntity entity = em.find(GameChatEntity.class, gameId);
        if (entity == null) return List.of();
        try {
            return mapper.readValue(entity.getHistory(), TURN_LIST);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize TurnHistory for game {}", gameId, e);
            return List.of();
        }
    }
}
