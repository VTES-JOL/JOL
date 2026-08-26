package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.PlayerEntity;
import net.deckserver.jpa.entity.RefreshTokenEntity;
import net.deckserver.storage.json.system.RefreshTokenInfo;

import java.util.List;

public class RefreshTokenRepository {

    public void save(EntityManager em, String playerName, RefreshTokenInfo info) {
        RefreshTokenEntity existing = em.find(RefreshTokenEntity.class, info.getId());
        if (existing != null) {
            existing.update(info);
            return;
        }
        PlayerEntity player = new PlayerRepository().findEntityByName(em, playerName);
        if (player == null) return;
        em.persist(RefreshTokenEntity.from(player.getPlayerId(), info));
    }

    public void delete(EntityManager em, String id) {
        RefreshTokenEntity entity = em.find(RefreshTokenEntity.class, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public void deleteAllForPlayer(EntityManager em, String playerName) {
        em.createQuery(
                "DELETE FROM RefreshTokenEntity t WHERE t.playerId IN " +
                "(SELECT p.playerId FROM PlayerEntity p WHERE p.playerName = :playerName)")
                .setParameter("playerName", playerName)
                .executeUpdate();
    }

    public void deleteExpired(EntityManager em, long now) {
        em.createQuery("DELETE FROM RefreshTokenEntity t WHERE t.expiresAt < :now")
                .setParameter("now", now)
                .executeUpdate();
    }

    public List<RefreshTokenEntity> findAll(EntityManager em) {
        return em.createQuery("SELECT t FROM RefreshTokenEntity t JOIN FETCH t.player", RefreshTokenEntity.class)
                .getResultList();
    }
}
