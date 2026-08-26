package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameInfoEntity;
import net.deckserver.jpa.entity.PlayerEntity;
import net.deckserver.jpa.entity.RegistrationEntity;
import net.deckserver.jpa.entity.RegistrationId;
import net.deckserver.storage.json.system.RegistrationStatus;

import java.util.List;

public class RegistrationRepository {

    public void save(EntityManager em, String gameName, String playerName, RegistrationStatus status) {
        GameInfoEntity game = em.find(GameInfoEntity.class, gameName);
        if (game == null) return;
        PlayerEntity player = new PlayerRepository().findEntityByName(em, playerName);
        if (player == null) return;
        RegistrationId pk = new RegistrationId(game.getGameId(), player.getPlayerId());
        RegistrationEntity existing = em.find(RegistrationEntity.class, pk);
        if (existing != null) {
            existing.update(status);
        } else {
            em.persist(RegistrationEntity.from(game.getGameId(), player.getPlayerId(), status));
        }
    }

    public void delete(EntityManager em, String gameName, String playerName) {
        GameInfoEntity game = em.find(GameInfoEntity.class, gameName);
        if (game == null) return;
        PlayerEntity player = new PlayerRepository().findEntityByName(em, playerName);
        if (player == null) return;
        RegistrationId pk = new RegistrationId(game.getGameId(), player.getPlayerId());
        RegistrationEntity entity = em.find(RegistrationEntity.class, pk);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public void deleteAllForGame(EntityManager em, String gameName) {
        em.createQuery(
                "DELETE FROM RegistrationEntity r WHERE r.id.gameId IN " +
                "(SELECT g.gameId FROM GameInfoEntity g WHERE g.gameName = :gameName)")
                .setParameter("gameName", gameName)
                .executeUpdate();
    }

    public List<RegistrationEntity> findAllForGame(EntityManager em, String gameName) {
        return em.createQuery(
                "SELECT r FROM RegistrationEntity r JOIN FETCH r.player JOIN FETCH r.game g LEFT JOIN FETCH g.owner WHERE g.gameName = :gameName",
                RegistrationEntity.class)
                .setParameter("gameName", gameName)
                .getResultList();
    }

    public List<RegistrationEntity> findAllForPlayer(EntityManager em, String playerName) {
        return em.createQuery(
                "SELECT r FROM RegistrationEntity r JOIN FETCH r.game g LEFT JOIN FETCH g.owner WHERE r.player.playerName = :playerName",
                RegistrationEntity.class)
                .setParameter("playerName", playerName)
                .getResultList();
    }

    public RegistrationEntity findByGameAndDeck(EntityManager em, String gameId, String deckId) {
        return em.createQuery(
                "SELECT r FROM RegistrationEntity r JOIN r.game g WHERE g.gameId = :gameId AND r.deckId = :deckId",
                RegistrationEntity.class)
                .setParameter("gameId", gameId)
                .setParameter("deckId", deckId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<RegistrationEntity> findAll(EntityManager em) {
        return em.createQuery("SELECT r FROM RegistrationEntity r JOIN FETCH r.player JOIN FETCH r.game", RegistrationEntity.class)
                .getResultList();
    }
}
