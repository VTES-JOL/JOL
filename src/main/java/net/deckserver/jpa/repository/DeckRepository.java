package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.DeckContentEntity;
import net.deckserver.jpa.entity.DeckInfoEntity;
import net.deckserver.jpa.entity.DeckInfoId;
import net.deckserver.jpa.entity.PlayerEntity;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.DeckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeckRepository {

    private static final Logger logger = LoggerFactory.getLogger(DeckRepository.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void saveDeckInfo(EntityManager em, String playerName, String deckName, DeckInfo info) {
        PlayerEntity player = new PlayerRepository().findEntityByName(em, playerName);
        if (player == null) return;
        DeckInfoId pk = new DeckInfoId(player.getPlayerId(), deckName);
        DeckInfoEntity existing = em.find(DeckInfoEntity.class, pk);
        if (existing != null) {
            em.merge(DeckInfoEntity.from(player.getPlayerId(), deckName, info));
        } else {
            em.persist(DeckInfoEntity.from(player.getPlayerId(), deckName, info));
        }
    }

    public void saveContent(EntityManager em, String deckId, ExtendedDeck deck) {
        try {
            String json = mapper.writeValueAsString(deck);
            DeckContentEntity existing = em.find(DeckContentEntity.class, deckId);
            if (existing != null) {
                existing.setContent(json);
                em.merge(existing);
            } else {
                em.persist(new DeckContentEntity(deckId, json));
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize deck content for {}", deckId, e);
        }
    }

    public void delete(EntityManager em, String playerName, String deckName) {
        PlayerEntity player = new PlayerRepository().findEntityByName(em, playerName);
        if (player == null) return;
        DeckInfoId pk = new DeckInfoId(player.getPlayerId(), deckName);
        DeckInfoEntity entity = em.find(DeckInfoEntity.class, pk);
        if (entity != null) {
            DeckContentEntity content = em.find(DeckContentEntity.class, entity.getDeckId());
            if (content != null) em.remove(content);
            em.remove(entity);
        }
    }

    /**
     * Raw column value, without parsing — legacy decks migrated from decks/&lt;id&gt;.txt
     * hold plain text here, not ExtendedDeck JSON.
     */
    public String findRawContent(EntityManager em, String deckId) {
        DeckContentEntity entity = em.find(DeckContentEntity.class, deckId);
        return entity != null ? entity.getContent() : null;
    }

    public ExtendedDeck findContent(EntityManager em, String deckId) {
        DeckContentEntity entity = em.find(DeckContentEntity.class, deckId);
        if (entity == null) return new ExtendedDeck();
        try {
            return mapper.readValue(entity.getContent(), ExtendedDeck.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize deck content for {}", deckId, e);
            return new ExtendedDeck();
        }
    }

    public List<DeckInfoEntity> findAllDeckInfos(EntityManager em) {
        return em.createQuery("SELECT d FROM DeckInfoEntity d", DeckInfoEntity.class).getResultList();
    }
}
