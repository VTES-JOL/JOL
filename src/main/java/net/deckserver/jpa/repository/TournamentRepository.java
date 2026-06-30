package net.deckserver.jpa.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.TournamentEntity;
import net.deckserver.jpa.entity.TournamentRegistrationEntity;
import net.deckserver.storage.json.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TournamentRepository {

    private static final Logger logger = LoggerFactory.getLogger(TournamentRepository.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(EntityManager em, TournamentDefinition tournament) {
        TournamentEntity entity = buildEntity(tournament);
        TournamentEntity existing = em.find(TournamentEntity.class, tournament.getId());
        if (existing != null) {
            em.merge(entity);
        } else {
            em.persist(entity);
        }

        // Replace all registrations for this tournament
        deleteRegistrations(em, tournament.getId());
        PlayerRepository playerRepo = new PlayerRepository();
        tournament.getRegistrations().forEach(reg -> {
            var player = playerRepo.findEntityByName(em, reg.getPlayer());
            if (player != null) {
                em.persist(TournamentRegistrationEntity.from(tournament.getId(), player.getPlayerId(), reg));
            }
        });
    }

    public void delete(EntityManager em, String tournamentId) {
        TournamentEntity entity = em.find(TournamentEntity.class, tournamentId);
        if (entity != null) {
            deleteRegistrations(em, tournamentId);
            em.remove(entity);
        }
    }

    public List<TournamentEntity> findAll(EntityManager em) {
        return em.createQuery("SELECT t FROM TournamentEntity t", TournamentEntity.class).getResultList();
    }

    public List<TournamentRegistrationEntity> findRegistrations(EntityManager em, String tournamentId) {
        return em.createQuery(
                        "SELECT r FROM TournamentRegistrationEntity r WHERE r.tournamentId = :id",
                        TournamentRegistrationEntity.class)
                .setParameter("id", tournamentId)
                .getResultList();
    }

    private void deleteRegistrations(EntityManager em, String tournamentId) {
        em.createQuery("DELETE FROM TournamentRegistrationEntity r WHERE r.tournamentId = :id")
                .setParameter("id", tournamentId)
                .executeUpdate();
    }

    private TournamentEntity buildEntity(TournamentDefinition t) {
        TournamentEntity entity = new TournamentEntity();
        entity.setTournamentId(t.getId());
        entity.setName(t.getName());
        entity.setRegistrationStart(t.getRegistrationStart());
        entity.setRegistrationEnd(t.getRegistrationEnd());
        entity.setPlayStarts(t.getPlayStarts());
        entity.setPlayEnds(t.getPlayEnds());
        entity.setFormat(t.getFormat());
        entity.setDeckFormat(t.getDeckFormat());
        entity.setNumberOfRounds(t.getNumberOfRounds());
        entity.setFinalEnabled(t.isFinalEnabled());
        entity.setRequiresId(t.isRequiresId());
        entity.setStatus(t.getStatus());
        entity.setRules(toJson(t.getRules()));
        entity.setSpecialRules(toJson(t.getSpecialRules()));
        entity.setRounds(toJson(t.getRounds()));
        entity.setFinals(toJson(t.getFinals()));
        return entity;
    }

    public TournamentDefinition toDefinition(TournamentEntity entity, List<TournamentRegistrationEntity> registrations) {
        TournamentDefinition def = new TournamentDefinition();
        def.setId(entity.getTournamentId());
        def.setName(entity.getName());
        def.setRegistrationStart(entity.getRegistrationStart());
        def.setRegistrationEnd(entity.getRegistrationEnd());
        def.setPlayStarts(entity.getPlayStarts());
        def.setPlayEnds(entity.getPlayEnds());
        def.setFormat(entity.getFormat());
        def.setDeckFormat(entity.getDeckFormat());
        def.setNumberOfRounds(entity.getNumberOfRounds());
        def.setFinalEnabled(entity.isFinalEnabled());
        def.setRequiresId(entity.isRequiresId());
        def.setStatus(entity.getStatus());
        fromJson(entity.getRules(), new TypeReference<List<String>>() {}, List.of())
                .forEach(r -> def.getRules().add(r));
        TournamentSpecialRules sr = fromJson(entity.getSpecialRules(),
                new TypeReference<TournamentSpecialRules>() {}, new TournamentSpecialRules());
        def.setSpecialRules(sr);
        Map<Integer, Map<Integer, List<TournamentPlayer>>> rounds = fromJson(
                entity.getRounds(),
                new TypeReference<Map<Integer, Map<Integer, List<TournamentPlayer>>>>() {}, Map.of());
        def.setRounds(rounds);
        TournamentFinals finals = fromJson(entity.getFinals(),
                new TypeReference<TournamentFinals>() {}, new TournamentFinals());
        def.setFinals(finals);
        registrations.forEach(r -> def.getRegistrations().add(r.toRegistration()));
        return def;
    }

    private String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize tournament field", e);
            return "null";
        }
    }

    private <T> T fromJson(String json, TypeReference<T> type, T defaultValue) {
        if (json == null || json.equals("null")) return defaultValue;
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize tournament field", e);
            return defaultValue;
        }
    }
}
