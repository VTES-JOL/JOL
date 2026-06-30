package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.PlayerActivityEntity;
import net.deckserver.jpa.entity.PlayerEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class PlayerActivityRepository {

    public void save(EntityManager em, String playerName, OffsetDateTime lastSeen) {
        PlayerEntity player = new PlayerRepository().findEntityByName(em, playerName);
        if (player == null) return;
        PlayerActivityEntity existing = em.find(PlayerActivityEntity.class, player.getPlayerId());
        if (existing != null) {
            existing.setLastSeen(lastSeen);
            em.merge(existing);
        } else {
            PlayerActivityEntity entity = new PlayerActivityEntity();
            entity.setPlayerId(player.getPlayerId());
            entity.setLastSeen(lastSeen);
            em.persist(entity);
        }
    }

    public void saveAll(EntityManager em, Map<String, OffsetDateTime> timestamps) {
        timestamps.forEach((name, ts) -> save(em, name, ts));
    }

    public List<PlayerActivityEntity> findAll(EntityManager em) {
        return em.createQuery("SELECT p FROM PlayerActivityEntity p JOIN FETCH p.player", PlayerActivityEntity.class)
                .getResultList();
    }
}
