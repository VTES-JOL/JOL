package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.game.model.JolGame;
import net.deckserver.jpa.entity.GameStateEntity;
import net.deckserver.storage.json.game.GameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class GameStateRepository {

    private static final Logger logger = LoggerFactory.getLogger(GameStateRepository.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(EntityManager em, JolGame game) {
        GameData data = game.data();
        String state;
        try {
            state = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize GameData for game " + game.id(), e);
        }

        GameStateEntity existing = em.find(GameStateEntity.class, game.id());
        GameStateEntity entity = existing != null ? existing : new GameStateEntity();
        entity.setGameId(game.id());
        entity.setState(state);
        entity.setCurrentPlayer(data.getCurrentPlayer() != null ? data.getCurrentPlayer().getName() : null);
        entity.setTurn(data.getTurn());
        entity.setPhase(data.getPhase() != null ? data.getPhase().name() : null);
        entity.setPlayerCount(data.getPlayerOrder().size());
        entity.setUpdatedAt(OffsetDateTime.now());

        if (existing == null) {
            em.persist(entity);
        }
    }

    public void delete(EntityManager em, String gameId) {
        GameStateEntity entity = em.find(GameStateEntity.class, gameId);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public GameData load(EntityManager em, String gameId) {
        GameStateEntity entity = em.find(GameStateEntity.class, gameId);
        if (entity == null) return null;
        try {
            return mapper.readValue(entity.getState(), GameData.class);
        } catch (JsonProcessingException e) {
            // must not be conflated with "no row" — callers treat null as a brand-new game
            throw new IllegalStateException("Failed to deserialize GameData for game " + gameId, e);
        }
    }
}
