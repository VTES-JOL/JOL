package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameCommandErrorEntity;
import net.deckserver.storage.json.game.CommandErrorData;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GameCommandErrorRepository {

    private static final DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");

    public void insert(EntityManager em, String gameId, String turnLabel,
                       String player, String rawCommand, String errorText) {
        OffsetDateTime now = OffsetDateTime.now();
        GameCommandErrorEntity entity = new GameCommandErrorEntity();
        entity.setGameId(gameId);
        entity.setTurnLabel(turnLabel != null ? turnLabel : "");
        entity.setOccurredAt(now);
        entity.setDisplayTs(now.format(SIMPLE_FORMAT));
        entity.setPlayer(player);
        entity.setRawCommand(rawCommand);
        entity.setErrorText(errorText);
        em.persist(entity);
    }

    public List<CommandErrorData> loadTurn(EntityManager em, String gameId, String turnLabel) {
        return em.createQuery(
                        "SELECT e FROM GameCommandErrorEntity e "
                                + "WHERE e.gameId = :gameId AND e.turnLabel = :turnLabel ORDER BY e.id",
                        GameCommandErrorEntity.class)
                .setParameter("gameId", gameId)
                .setParameter("turnLabel", turnLabel)
                .getResultList()
                .stream()
                .map(GameCommandErrorRepository::toData)
                .toList();
    }

    public void deleteForGame(EntityManager em, String gameId) {
        em.createQuery("DELETE FROM GameCommandErrorEntity e WHERE e.gameId = :gameId")
                .setParameter("gameId", gameId)
                .executeUpdate();
    }

    private static CommandErrorData toData(GameCommandErrorEntity e) {
        CommandErrorData d = new CommandErrorData();
        d.setTimestamp(e.getDisplayTs());
        d.setOccurredAt(e.getOccurredAt() != null ? e.getOccurredAt().toString() : null);
        d.setPlayer(e.getPlayer());
        d.setCommand(e.getRawCommand());
        d.setError(e.getErrorText());
        return d;
    }
}
