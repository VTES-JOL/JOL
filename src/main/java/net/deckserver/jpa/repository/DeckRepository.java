package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.DeckContentEntity;
import net.deckserver.jpa.entity.DeckInfoEntity;
import net.deckserver.jpa.entity.DeckInfoId;
import net.deckserver.jpa.entity.PlayerEntity;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.DeckNormalizer;
import net.deckserver.storage.json.deck.DeckParser;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.DeckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            existing.update(info);
        } else {
            em.persist(DeckInfoEntity.from(player.getPlayerId(), deckName, info));
        }
    }

    public void saveContent(EntityManager em, String deckId, ExtendedDeck deck) {
        try {
            // Persist only the canonical Deck model — the derived stats/errors
            // in ExtendedDeck are recomputed on read by findContent(), never
            // stored (they go stale against the card database otherwise).
            Deck canonical = deck.getDeck() != null ? deck.getDeck() : new Deck();
            String json = mapper.writeValueAsString(canonical);
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

    /**
     * Loads a deck's content and returns it with freshly-computed
     * {@code stats}/{@code errors}. {@link DeckNormalizer} tolerates every
     * historical stored shape (bare {@link Deck} JSON, the old
     * {@code {"deck":…}} ExtendedDeck JSON, KRCG JSON, legacy text), so rows
     * written before the canonical-Deck switch still read correctly.
     */
    public ExtendedDeck findContent(EntityManager em, String deckId) {
        DeckContentEntity entity = em.find(DeckContentEntity.class, deckId);
        if (entity == null) return new ExtendedDeck();
        return DeckParser.analyze(DeckNormalizer.normalize(entity.getContent()));
    }

    public DeckInfo findByPlayerAndName(EntityManager em, String playerName, String deckName) {
        return em.createQuery(
                        "SELECT d FROM DeckInfoEntity d JOIN FETCH d.player p " +
                        "WHERE p.playerName = :playerName AND d.id.deckName = :deckName",
                        DeckInfoEntity.class)
                .setParameter("playerName", playerName)
                .setParameter("deckName", deckName)
                .getResultStream()
                .findFirst()
                .map(DeckInfoEntity::toDeckInfo)
                .orElse(null);
    }

    public Map<String, DeckInfo> findByPlayerName(EntityManager em, String playerName) {
        Map<String, DeckInfo> result = new LinkedHashMap<>();
        em.createQuery(
                        "SELECT d FROM DeckInfoEntity d JOIN FETCH d.player p " +
                        "WHERE p.playerName = :playerName",
                        DeckInfoEntity.class)
                .setParameter("playerName", playerName)
                .getResultList()
                .forEach(e -> result.put(e.getId().getDeckName(), e.toDeckInfo()));
        return result;
    }

    public List<DeckInfoEntity> findAllDeckInfos(EntityManager em) {
        return em.createQuery("SELECT d FROM DeckInfoEntity d", DeckInfoEntity.class).getResultList();
    }
}
