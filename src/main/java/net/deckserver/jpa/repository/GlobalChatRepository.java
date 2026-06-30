package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.jpa.entity.GlobalChatEntity;
import net.deckserver.jpa.entity.PlayerEntity;

import java.time.OffsetDateTime;
import java.util.List;

public class GlobalChatRepository {

    private static final int KEEP_ROWS = 1000;

    public void insert(EntityManager em, ChatEntryBean chat) {
        PlayerEntity player = new PlayerRepository().findEntityByName(em, chat.getPlayer());
        if (player == null) return;
        GlobalChatEntity entity = new GlobalChatEntity();
        entity.setPlayerId(player.getPlayerId());
        entity.setMessage(chat.getMessage());
        entity.setPostedAt(OffsetDateTime.now());
        em.persist(entity);
    }

    public void trim(EntityManager em) {
        long count = em.createQuery("SELECT COUNT(c) FROM GlobalChatEntity c", Long.class)
                .getSingleResult();
        if (count <= KEEP_ROWS) return;

        Long cutoffId = em.createQuery(
                        "SELECT c.id FROM GlobalChatEntity c ORDER BY c.id DESC", Long.class)
                .setFirstResult(KEEP_ROWS - 1)
                .setMaxResults(1)
                .getSingleResult();

        em.createQuery("DELETE FROM GlobalChatEntity c WHERE c.id < :cutoff")
                .setParameter("cutoff", cutoffId)
                .executeUpdate();
    }

    public List<GlobalChatEntity> findRecent(EntityManager em, int limit) {
        return em.createQuery(
                        "SELECT c FROM GlobalChatEntity c ORDER BY c.id DESC", GlobalChatEntity.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
