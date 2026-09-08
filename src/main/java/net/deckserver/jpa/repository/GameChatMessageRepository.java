package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.entity.GameChatMessageEntity;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.TurnData;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Row-per-message persistence for game chat, replacing {@link GameChatRepository}'s
 * single-blob {@code save}/{@code load}. Style matches {@code GlobalChatRepository}:
 * plain {@code EntityManager} calls, no framework.
 */
public class GameChatMessageRepository {

    /** Persist one chat line. Caller supplies the turn grouping (denormalised onto the row). */
    public void insert(EntityManager em, String gameId, int turnSeq, int chatSeq,
                       String turnId, String turnPlayer, String turnLabel, ChatData chat) {
        GameChatMessageEntity entity = new GameChatMessageEntity();
        entity.setGameId(gameId);
        entity.setTurnSeq(turnSeq);
        entity.setChatSeq(chatSeq);
        entity.setTurnId(turnId);
        entity.setTurnPlayer(turnPlayer);
        entity.setTurnLabel(turnLabel);
        entity.setPostedAt(OffsetDateTime.now());
        entity.setDisplayTs(chat.getTimestamp());
        entity.setSource(chat.getSource());
        entity.setMessage(chat.getMessage() != null ? chat.getMessage() : "");
        entity.setCommand(chat.getCommand());
        entity.setInvocation(chat.getInvocation());
        entity.setInvocationBy(chat.getInvocationBy());
        entity.setInvocationSeq(chat.getInvocationSeq());
        em.persist(entity);
    }

    /** All messages for a game, folded back into ordered {@link TurnData} the way {@code TurnHistory} expects. */
    public List<TurnData> load(EntityManager em, String gameId) {
        List<GameChatMessageEntity> rows = em.createQuery(
                        "SELECT m FROM GameChatMessageEntity m WHERE m.gameId = :gameId "
                                + "ORDER BY m.turnSeq, m.chatSeq, m.id", GameChatMessageEntity.class)
                .setParameter("gameId", gameId)
                .getResultList();

        List<TurnData> turns = new ArrayList<>();
        TurnData current = null;
        String currentLabel = null;
        for (GameChatMessageEntity row : rows) {
            if (!row.getTurnLabel().equals(currentLabel)) {
                current = new TurnData(row.getTurnPlayer(), row.getTurnId());
                turns.add(current);
                currentLabel = row.getTurnLabel();
            }
            current.addChat(toChatData(row));
        }
        return turns;
    }

    /**
     * Latest {@code posted_at} per {@code source} for a game — used to backfill
     * {@code PlayerData.lastActionAt} on load for games whose state blob predates
     * that field (see {@code GameService.loadGame}). {@code source} is the actor
     * name for a command / chat line ("SYSTEM" / "Judge - X" rows are returned
     * too; the caller keeps only keys that match a seated player).
     */
    public java.util.Map<String, java.time.OffsetDateTime> lastActivityBySource(EntityManager em, String gameId) {
        List<Object[]> rows = em.createQuery(
                        "SELECT m.source, MAX(m.postedAt) FROM GameChatMessageEntity m "
                                + "WHERE m.gameId = :gameId AND m.source IS NOT NULL "
                                + "GROUP BY m.source", Object[].class)
                .setParameter("gameId", gameId)
                .getResultList();
        java.util.Map<String, java.time.OffsetDateTime> result = new java.util.HashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (java.time.OffsetDateTime) row[1]);
        }
        return result;
    }

    public void deleteForGame(EntityManager em, String gameId) {
        em.createQuery("DELETE FROM GameChatMessageEntity m WHERE m.gameId = :gameId")
                .setParameter("gameId", gameId)
                .executeUpdate();
    }

    private static ChatData toChatData(GameChatMessageEntity row) {
        ChatData chat = new ChatData();
        chat.setTimestamp(row.getDisplayTs());
        chat.setMessage(row.getMessage());
        chat.setSource(row.getSource());
        chat.setCommand(row.getCommand());
        chat.setInvocation(row.getInvocation());
        chat.setInvocationBy(row.getInvocationBy());
        chat.setInvocationSeq(row.getInvocationSeq());
        chat.setPostedAt(row.getPostedAt() != null ? row.getPostedAt().toString() : null);
        return chat;
    }
}
