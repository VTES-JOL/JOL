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
        notifyGame(gameId, null, -1L);
    }

    public static void notifyGame(String gameId, long stamp) {
        notifyGame(gameId, null, stamp);
    }

    /**
     * Room-scoped game-update push. Skips the single WS session tagged with
     * excludeClientId — a caller whose own REST response already carries the
     * fresh game state doesn't need its own action to also trigger a
     * self-refetch race. Every other session watching this game, including that
     * same player's other tabs, is unaffected.
     *
     * Uses the same {"type":"invalidate","key":[...]} envelope as
     * notifyInvalidate, room-scoped to gameSessions — ws/useQueryInvalidation.ts's
     * generic bridge handles it like any other push. In addition it carries
     * {@code "stamp": <n>}: the per-game monotonic snapshot version (see
     * JolAdmin.getGameStamp / bumpGameStamp). A client whose cached
     * GameSnapshot.stamp is already ≥ this value can skip the refetch (D8
     * optimistic-UI enabler). {@code stamp < 0} means "unknown — always refetch".
     */
    public static void notifyGame(String gameId, String excludeClientId, long stamp) {
        Session exclude = excludeClientId == null ? null : clientSessions.get(excludeClientId);
        String message = "{\"type\":\"invalidate\",\"key\":" + toJsonArray(List.of("game", gameId))
                + ",\"stamp\":" + stamp + "}";
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

    // Fire-and-forget: getAsyncRemote().sendText queues the frame and returns
    // immediately instead of blocking the caller until the socket drains. This
    // matters because notifyGame/notifyInvalidate run inside GameModel.lock (via
    // JolAdmin.saveGameState) — a single slow / backpressured consumer must not
    // stall every other player's mutation for that game. Delivery failures for a
    // dead session are reported to the SendHandler and evicted there.
    private static void send(Session session, String message) {
        try {
            if (!session.isOpen()) {
                evict(session);
                return;
            }
            session.getAsyncRemote().sendText(message, result -> {
                if (!result.isOK()) {
                    Throwable ex = result.getException();
                    log.warn("WebSocket async send failed for session {}, removing: {}",
                            session.getId(), ex == null ? "unknown error" : ex.getMessage());
                    evict(session);
                }
            });
        } catch (Exception e) {
            // A synchronous throw here (e.g. IllegalStateException if a prior async write is
            // still in progress and the impl doesn't queue) does NOT mean the session is dead
            // — only the SendHandler above can tell us that. Log and drop this one frame; the
            // client re-syncs on its next REST fetch / reconnect anyway.
            log.warn("WebSocket async send threw for session {}, dropping frame: {}", session.getId(), e.getMessage());
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
