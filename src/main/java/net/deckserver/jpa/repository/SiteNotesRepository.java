package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.SiteNotesEntity;

public class SiteNotesRepository {

    public String load(EntityManager em) {
        SiteNotesEntity entity = em.find(SiteNotesEntity.class, SiteNotesEntity.SINGLETON_ID);
        return entity != null ? entity.getNotes() : "";
    }

    public void save(EntityManager em, String notes) {
        SiteNotesEntity entity = em.find(SiteNotesEntity.class, SiteNotesEntity.SINGLETON_ID);
        if (entity == null) {
            entity = new SiteNotesEntity();
        }
        entity.setNotes(notes);
        em.merge(entity);
    }
}
