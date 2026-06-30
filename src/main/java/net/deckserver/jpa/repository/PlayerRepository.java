package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.PlayerEntity;
import net.deckserver.storage.json.system.PlayerInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerRepository {

    public PlayerEntity findEntityByName(EntityManager em, String playerName) {
        List<PlayerEntity> results = em.createQuery(
                        "SELECT p FROM PlayerEntity p WHERE p.playerName = :name", PlayerEntity.class)
                .setParameter("name", playerName)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public PlayerInfo findByName(EntityManager em, String name) {
        PlayerEntity entity = findEntityByName(em, name);
        return entity != null ? entity.toPlayerInfo() : null;
    }

    public void save(EntityManager em, PlayerInfo player) {
        PlayerEntity existing = em.find(PlayerEntity.class, player.getId());
        if (existing != null) {
            em.merge(PlayerEntity.from(player));
        } else {
            em.persist(PlayerEntity.from(player));
        }
    }

    public void delete(EntityManager em, String playerName) {
        PlayerEntity entity = findEntityByName(em, playerName);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public Map<String, PlayerInfo> findAll(EntityManager em) {
        List<PlayerEntity> entities = em.createQuery("SELECT p FROM PlayerEntity p", PlayerEntity.class)
                .getResultList();
        return entities.stream()
                .collect(Collectors.toMap(PlayerEntity::getPlayerName, PlayerEntity::toPlayerInfo));
    }
}
