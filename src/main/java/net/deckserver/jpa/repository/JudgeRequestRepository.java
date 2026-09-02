package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.game.enums.JudgeRequestCategory;
import net.deckserver.game.enums.JudgeRequestStatus;
import net.deckserver.jpa.entity.JudgeRequestEntity;
import net.deckserver.storage.json.game.JudgeRequestData;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Plain {@code EntityManager} persistence for {@code judge_request}, style
 * matching {@link GameCommandErrorRepository}. Reads map rows to
 * {@link JudgeRequestData}; entities never leave this class.
 */
public class JudgeRequestRepository {

    /** Insert a fresh OPEN request; returns the row (id populated). */
    public JudgeRequestData insert(EntityManager em, String gameId, String gameName, String tournamentName,
                                   String requestedBy, JudgeRequestCategory category,
                                   String rawDetails, String parsedDetails) {
        OffsetDateTime now = OffsetDateTime.now();
        JudgeRequestEntity entity = new JudgeRequestEntity();
        entity.setGameId(gameId);
        entity.setGameName(gameName);
        entity.setTournamentName(tournamentName);
        entity.setRequestedBy(requestedBy);
        entity.setCategory(category);
        entity.setStatus(JudgeRequestStatus.OPEN);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setRawDetails(rawDetails);
        entity.setParsedDetails(parsedDetails);
        em.persist(entity);
        em.flush(); // populate the generated id
        return toData(entity);
    }

    public JudgeRequestData findById(EntityManager em, long id) {
        JudgeRequestEntity entity = em.find(JudgeRequestEntity.class, id);
        return entity != null ? toData(entity) : null;
    }

    public JudgeRequestData findOpenForGame(EntityManager em, String gameId) {
        return em.createQuery(
                        "SELECT e FROM JudgeRequestEntity e "
                                + "WHERE e.gameId = :gameId AND e.status = :open", JudgeRequestEntity.class)
                .setParameter("gameId", gameId)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .getResultList()
                .stream()
                .findFirst()
                .map(JudgeRequestRepository::toData)
                .orElse(null);
    }

    /** Edit the details of an OPEN request. Returns rows updated (0 if it is no longer OPEN). */
    public int updateDetails(EntityManager em, long id, JudgeRequestCategory category,
                             String rawDetails, String parsedDetails) {
        return em.createQuery(
                        "UPDATE JudgeRequestEntity e SET e.category = :category, e.rawDetails = :raw, "
                                + "e.parsedDetails = :parsed, e.updatedAt = :now "
                                + "WHERE e.id = :id AND e.status = :open")
                .setParameter("category", category)
                .setParameter("raw", rawDetails)
                .setParameter("parsed", parsedDetails)
                .setParameter("now", OffsetDateTime.now())
                .setParameter("id", id)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .executeUpdate();
    }

    /** Retract an OPEN request. Returns rows updated (0 if it is no longer OPEN). */
    public int retract(EntityManager em, long id) {
        return em.createQuery(
                        "UPDATE JudgeRequestEntity e SET e.status = :retracted, e.updatedAt = :now "
                                + "WHERE e.id = :id AND e.status = :open")
                .setParameter("retracted", JudgeRequestStatus.RETRACTED)
                .setParameter("now", OffsetDateTime.now())
                .setParameter("id", id)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .executeUpdate();
    }

    /**
     * Resolve an OPEN request. The {@code AND e.status = OPEN} guard makes this
     * first-to-resolve-wins: a second concurrent resolve updates 0 rows.
     */
    public int resolve(EntityManager em, long id, String resolvedBy,
                       String resolutionRaw, String resolutionParsed) {
        OffsetDateTime now = OffsetDateTime.now();
        return em.createQuery(
                        "UPDATE JudgeRequestEntity e SET e.status = :resolved, e.resolvedBy = :by, "
                                + "e.resolvedAt = :now, e.resolutionRaw = :raw, e.resolutionParsed = :parsed, "
                                + "e.updatedAt = :now WHERE e.id = :id AND e.status = :open")
                .setParameter("resolved", JudgeRequestStatus.RESOLVED)
                .setParameter("by", resolvedBy)
                .setParameter("now", now)
                .setParameter("raw", resolutionRaw)
                .setParameter("parsed", resolutionParsed)
                .setParameter("id", id)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .executeUpdate();
    }

    /** Open queue: oldest first. Excludes rows whose game has since been deleted. */
    public List<JudgeRequestData> listOpen(EntityManager em) {
        return em.createQuery(
                        "SELECT e FROM JudgeRequestEntity e "
                                + "WHERE e.status = :open AND e.gameId IS NOT NULL "
                                + "ORDER BY e.createdAt ASC", JudgeRequestEntity.class)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .getResultList()
                .stream()
                .map(JudgeRequestRepository::toData)
                .toList();
    }

    /** Ruling history: most recently resolved first. */
    public List<JudgeRequestData> listResolved(EntityManager em, int limit) {
        return em.createQuery(
                        "SELECT e FROM JudgeRequestEntity e WHERE e.status = :resolved "
                                + "ORDER BY e.resolvedAt DESC", JudgeRequestEntity.class)
                .setParameter("resolved", JudgeRequestStatus.RESOLVED)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(JudgeRequestRepository::toData)
                .toList();
    }

    public long countOpen(EntityManager em) {
        return em.createQuery(
                        "SELECT COUNT(e) FROM JudgeRequestEntity e "
                                + "WHERE e.status = :open AND e.gameId IS NOT NULL", Long.class)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .getSingleResult();
    }

    /**
     * Flip any still-OPEN request for a game being deleted to RETRACTED, so the
     * history row survives (game_id becomes NULL via the FK) without lingering
     * as a phantom OPEN entry.
     */
    public void abandonOpenForGame(EntityManager em, String gameId) {
        em.createQuery(
                        "UPDATE JudgeRequestEntity e SET e.status = :retracted, e.updatedAt = :now "
                                + "WHERE e.gameId = :gameId AND e.status = :open")
                .setParameter("retracted", JudgeRequestStatus.RETRACTED)
                .setParameter("now", OffsetDateTime.now())
                .setParameter("gameId", gameId)
                .setParameter("open", JudgeRequestStatus.OPEN)
                .executeUpdate();
    }

    private static JudgeRequestData toData(JudgeRequestEntity e) {
        JudgeRequestData d = new JudgeRequestData();
        d.setId(e.getId());
        d.setGameId(e.getGameId());
        d.setGameName(e.getGameName());
        d.setTournamentName(e.getTournamentName());
        d.setRequestedBy(e.getRequestedBy());
        d.setCategory(e.getCategory());
        d.setStatus(e.getStatus());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setRawDetails(e.getRawDetails());
        d.setParsedDetails(e.getParsedDetails());
        d.setResolvedBy(e.getResolvedBy());
        d.setResolvedAt(e.getResolvedAt());
        d.setResolutionRaw(e.getResolutionRaw());
        d.setResolutionParsed(e.getResolutionParsed());
        return d;
    }
}
