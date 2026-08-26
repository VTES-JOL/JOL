package net.deckserver.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.websocket.Session;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketRegistry {

    private static final Logger log = LoggerFactory.getLogger(WebSocketRegistry.class);
    private static final String CLIENT_ID_KEY = "clientId";
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> sessions = new ConcurrentHashMap<>();
    // gameId -> sessions watching that game
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> gameSessions = new ConcurrentHashMap<>();
    // Browser-tab-generated id (see frontend/src/ws/socket.ts) -> its WS session, so a REST
    // call from that same tab can be excluded from a broadcast it triggered — a player-name
    // based exclusion would be wrong here, since it would also skip that player's *other* tabs,
    // which never saw the REST response and still need the notification.
    private static final ConcurrentHashMap<String, Session> clientSessions = new ConcurrentHashMap<>();

    public static void register(String playerName, Session session) {
        sessions.computeIfAbsent(playerName, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public static void unregister(String playerName, Session session) {
        CopyOnWriteArraySet<Session> s = sessions.get(playerName);
        if (s != null) {
            s.remove(session);
            if (s.isEmpty()) sessions.remove(playerName);
        }
        // remove from any game room this session was watching; evict empty sets
        gameSessions.entrySet().removeIf(entry -> {
            entry.getValue().remove(session);
            return entry.getValue().isEmpty();
        });
        unregisterClientId(session);
    }

    public static void registerClientId(String clientId, Session session) {
        session.getUserProperties().put(CLIENT_ID_KEY, clientId);
        clientSessions.put(clientId, session);
    }

    private static void unregisterClientId(Session session) {
        Object clientId = session.getUserProperties().get(CLIENT_ID_KEY);
        if (clientId != null) {
            clientSessions.remove(clientId);
        }
    }

    public static void joinGame(String gameId, Session session) {
        gameSessions.computeIfAbsent(gameId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public static void leaveGame(String gameId, Session session) {
        CopyOnWriteArraySet<Session> s = gameSessions.get(gameId);
        if (s != null) {
            s.remove(session);
            if (s.isEmpty()) gameSessions.remove(gameId);
        }
    }

    public static void notifyGame(String gameId) {
        notifyGame(gameId, null);
    }

    /**
     * Same as notifyGame(gameId), but skips the single WS session tagged with
     * excludeClientId — mirrors notifyInvalidate(key, excludeClientId): a
     * caller whose own REST response already carries the fresh game state
     * doesn't need its own action to also trigger a self-refetch race. Every
     * other session watching this game, including that same player's other
     * tabs, is unaffected.
     *
     * Uses the same {"type":"invalidate","key":[...]} envelope as
     * notifyInvalidate (see below), just room-scoped to gameSessions instead
     * of broadcast to every session — ws/useQueryInvalidation.ts's generic
     * bridge handles this the same way it handles Lobby's pushes, with no
     * per-page message-type handling needed. Replaces the old
     * {"type":"game","id":...} shape outright (ws/useGameSocket.ts was its
     * only consumer, so there's nothing else to keep working).
     */
    public static void notifyGame(String gameId, String excludeClientId) {
        Session exclude = excludeClientId == null ? null : clientSessions.get(excludeClientId);
        String message = "{\"type\":\"invalidate\",\"key\":" + toJsonArray(List.of("game", gameId)) + "}";
        CopyOnWriteArraySet<Session> targets = gameSessions.get(gameId);
        if (targets != null) {
            targets.forEach(session -> {
                if (session != exclude) send(session, message);
            });
        }
    }

    /**
     * TanStack-Query-friendly push: carries the query key itself, so the
     * frontend bridge (ws/useQueryInvalidation.ts) is a direct
     * queryClient.invalidateQueries({queryKey: key}) with no scope-string
     * lookup table. Broadcasts to every session — for anything room-scoped
     * (e.g. a single game), see notifyGame instead, which uses the same
     * envelope but targets gameSessions.
     */
    public static void notifyInvalidate(List<String> key) {
        notifyInvalidate(key, null);
    }

    public static void notifyInvalidate(List<String> key, String excludeClientId) {
        Session exclude = excludeClientId == null ? null : clientSessions.get(excludeClientId);
        String message = "{\"type\":\"invalidate\",\"key\":" + toJsonArray(key) + "}";
        sessions.values().forEach(set -> set.forEach(session -> {
            if (session != exclude) send(session, message);
        }));
    }

    private static String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(items.get(i)).append("\"");
        }
        return sb.append("]").toString();
    }

    private static void send(Session session, String message) {
        try {
            if (session.isOpen()) session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.warn("WebSocket send failed for session {}, removing: {}", session.getId(), e.getMessage());
            evict(session);
        }
    }

    private static void evict(Session session) {
        sessions.values().forEach(set -> set.remove(session));
        sessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        gameSessions.entrySet().removeIf(entry -> {
            entry.getValue().remove(session);
            return entry.getValue().isEmpty();
        });
        unregisterClientId(session);
    }
}
