package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.jpa.entity.DeckFormatValidityEntity;
import net.deckserver.jpa.entity.DeckFormatValidityId;
import net.deckserver.storage.json.deck.DeckValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeckFormatValidityRepository {

    private static final Logger logger = LoggerFactory.getLogger(DeckFormatValidityRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    public void upsert(EntityManager em, String deckId, GameFormat format, boolean valid,
                       List<String> errors, OffsetDateTime computedAt) {
        DeckFormatValidityId id = new DeckFormatValidityId(deckId, format);
        DeckFormatValidityEntity entity = em.find(DeckFormatValidityEntity.class, id);
        if (entity == null) {
            entity = new DeckFormatValidityEntity();
            entity.setId(id);
        }
        entity.setValid(valid);
        entity.setErrors(writeErrors(errors));
        entity.setComputedAt(computedAt);
        em.merge(entity);
    }

    public Map<GameFormat, DeckValidity> findByDeck(EntityManager em, String deckId) {
        Map<GameFormat, DeckValidity> result = new EnumMap<>(GameFormat.class);
        em.createQuery("SELECT v FROM DeckFormatValidityEntity v WHERE v.id.deckId = :deckId", DeckFormatValidityEntity.class)
                .setParameter("deckId", deckId)
                .getResultList()
                .forEach(entity -> result.put(entity.getId().getFormat(), toDomain(entity)));
        return result;
    }

    public Optional<DeckValidity> findByDeckAndFormat(EntityManager em, String deckId, GameFormat format) {
        return Optional.ofNullable(em.find(DeckFormatValidityEntity.class, new DeckFormatValidityId(deckId, format)))
                .map(this::toDomain);
    }

    public void deleteByDeck(EntityManager em, String deckId) {
        em.createQuery("DELETE FROM DeckFormatValidityEntity v WHERE v.id.deckId = :deckId")
                .setParameter("deckId", deckId)
                .executeUpdate();
    }

    private DeckValidity toDomain(DeckFormatValidityEntity entity) {
        return new DeckValidity(entity.getId().getFormat(), entity.isValid(), readErrors(entity.getErrors()), entity.getComputedAt());
    }

    private String writeErrors(List<String> errors) {
        try {
            return MAPPER.writeValueAsString(errors == null ? List.of() : errors);
        } catch (Exception e) {
            logger.error("Failed to serialize deck validity errors", e);
            return "[]";
        }
    }

    private List<String> readErrors(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, STRING_LIST);
        } catch (Exception e) {
            logger.error("Failed to deserialize deck validity errors: {}", json, e);
            return List.of();
        }
    }
}
