package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameInfoEntity;
import net.deckserver.jpa.entity.PlayerEntity;
import net.deckserver.storage.json.system.GameInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameInfoRepository {

    public void save(EntityManager em, GameInfo info) {
        String ownerId = null;
        if (info.getOwner() != null) {
            PlayerEntity owner = new PlayerRepository().findEntityByName(em, info.getOwner());
            if (owner != null) ownerId = owner.getPlayerId();
        }
        GameInfoEntity existing = em.find(GameInfoEntity.class, info.getName());
        if (existing != null) {
            em.merge(GameInfoEntity.from(info, ownerId));
        } else {
            em.persist(GameInfoEntity.from(info, ownerId));
        }
    }

    public void delete(EntityManager em, String gameName) {
        GameInfoEntity entity = em.find(GameInfoEntity.class, gameName);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public Map<String, GameInfo> findAll(EntityManager em) {
        List<GameInfoEntity> entities = em.createQuery("SELECT g FROM GameInfoEntity g", GameInfoEntity.class)
                .getResultList();
        return entities.stream()
                .collect(Collectors.toMap(GameInfoEntity::getGameName, GameInfoEntity::toGameInfo));
    }
}
