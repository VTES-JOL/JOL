package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.SubscriptionEntity;
import net.deckserver.push.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionRepository {

    public Map<String, List<Subscription>> findAll(EntityManager em) {
        List<SubscriptionEntity> entities = em.createQuery(
                        "SELECT s FROM SubscriptionEntity s JOIN FETCH s.player", SubscriptionEntity.class)
                .getResultList();
        Map<String, List<Subscription>> result = new HashMap<>();
        for (SubscriptionEntity entity : entities) {
            result.computeIfAbsent(entity.getPlayerName(), name -> new ArrayList<>())
                    .add(toSubscription(entity));
        }
        return result;
    }

    public void upsert(EntityManager em, String playerName, Subscription subscription) {
        SubscriptionEntity entity = findEntity(em, playerName, subscription.getEndpoint());
        if (entity == null) {
            entity = new SubscriptionEntity();
            entity.setPlayerId(new PlayerRepository().findEntityByName(em, playerName).getPlayerId());
            entity.setEndpoint(subscription.getEndpoint());
        }
        entity.setAuthKey(subscription.getAuth());
        entity.setP256dhKey(subscription.getKey());
        entity.setFailureCount(subscription.getFailureCount());
        em.merge(entity);
    }

    public void delete(EntityManager em, String playerName, String endpoint) {
        SubscriptionEntity entity = findEntity(em, playerName, endpoint);
        if (entity != null) {
            em.remove(entity);
        }
    }

    private SubscriptionEntity findEntity(EntityManager em, String playerName, String endpoint) {
        List<SubscriptionEntity> results = em.createQuery(
                        "SELECT s FROM SubscriptionEntity s JOIN s.player p WHERE p.playerName = :name AND s.endpoint = :endpoint",
                        SubscriptionEntity.class)
                .setParameter("name", playerName)
                .setParameter("endpoint", endpoint)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private Subscription toSubscription(SubscriptionEntity entity) {
        Subscription subscription = new Subscription();
        subscription.setAuth(entity.getAuthKey());
        subscription.setKey(entity.getP256dhKey());
        subscription.setEndpoint(entity.getEndpoint());
        subscription.setFailureCount(entity.getFailureCount());
        return subscription;
    }
}
