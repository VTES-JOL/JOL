package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameActivityEntity;
import net.deckserver.jpa.entity.GameInfoEntity;
import net.deckserver.storage.json.game.GameTimestampEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivityRepository {

    private static final Logger logger = LoggerFactory.getLogger(GameActivityRepository.class);
    private static final ObjectMapper mapper;
    private static final TypeReference<Map<String, OffsetDateTime>> TS_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Boolean>> PING_TYPE = new TypeReference<>() {};

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(EntityManager em, String gameName, GameTimestampEntry entry) {
        GameInfoEntity game = em.find(GameInfoEntity.class, gameName);
        if (game == null) return;
        GameActivityEntity existing = em.find(GameActivityEntity.class, game.getGameId());
        GameActivityEntity entity = existing != null ? existing : new GameActivityEntity();
        entity.setGameId(game.getGameId());
        entity.setLastUpdated(entry.getTimestamp());
        entity.setPlayerTimestamps(toJson(entry.getPlayerTimestamps()));
        entity.setPlayerPings(toJson(entry.getPlayerPings()));
        if (existing != null) {
            em.merge(entity);
        } else {
            em.persist(entity);
        }
    }

    public void saveAll(EntityManager em, Map<String, GameTimestampEntry> entries) {
        entries.forEach((name, entry) -> save(em, name, entry));
    }

    public void delete(EntityManager em, String gameName) {
        GameInfoEntity game = em.find(GameInfoEntity.class, gameName);
        if (game == null) return;
        GameActivityEntity entity = em.find(GameActivityEntity.class, game.getGameId());
        if (entity != null) em.remove(entity);
    }

    public Map<String, GameTimestampEntry> findAllAsMap(EntityManager em) {
        List<GameActivityEntity> entities = em.createQuery(
                        "SELECT g FROM GameActivityEntity g", GameActivityEntity.class)
                .getResultList();
        Map<String, GameTimestampEntry> result = new HashMap<>();
        for (GameActivityEntity entity : entities) {
            String name = entity.getGameName();
            if (name == null) continue;
            GameTimestampEntry entry = new GameTimestampEntry();
            entry.setTimestamp(entity.getLastUpdated());
            entry.setPlayerTimestamps(fromJson(entity.getPlayerTimestamps(), TS_TYPE, new HashMap<>()));
            entry.setPlayerPings(fromJson(entity.getPlayerPings(), PING_TYPE, new HashMap<>()));
            result.put(name, entry);
        }
        return result;
    }

    private <T> T fromJson(String json, TypeReference<T> type, T defaultValue) {
        if (json == null || json.equals("null") || json.isBlank()) return defaultValue;
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize game activity field", e);
            return defaultValue;
        }
    }

    private String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize game activity field", e);
            return "{}";
        }
    }
}
