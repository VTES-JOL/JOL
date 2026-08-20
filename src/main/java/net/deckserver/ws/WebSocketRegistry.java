package net.deckserver.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.websocket.Session;
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
        String message = "{\"type\":\"game\",\"id\":\"" + gameId + "\"}";
        CopyOnWriteArraySet<Session> targets = gameSessions.get(gameId);
        if (targets != null) {
            targets.forEach(session -> send(session, message));
        }
    }

    public static void notifyMain() {
        broadcast("{\"type\":\"main\"}");
    }

    /**
     * Scoped sibling of notifyMain() — always fired alongside it, never
     * instead of it, so unconverted views relying on the generic "main"
     * signal are unaffected. Lets React widgets subscribe to only the slice
     * that actually changed (e.g. "chat", "games", "notes") instead of
     * refetching everything on every unrelated update.
     */
    public static void notifyMainScope(String scope) {
        notifyMainScope(scope, null);
    }

    /**
     * Same as notifyMainScope(scope), but skips the single WS session tagged
     * with excludeClientId — for a caller whose own REST response already
     * carries the fresh state, so its own re-notification would just be a
     * redundant, always-empty-or-stale-by-definition refetch. Every *other*
     * session, including that same player's other tabs, is unaffected.
     */
    public static void notifyMainScope(String scope, String excludeClientId) {
        Session exclude = excludeClientId == null ? null : clientSessions.get(excludeClientId);
        String message = "{\"type\":\"main:" + scope + "\"}";
        sessions.values().forEach(set -> set.forEach(session -> {
            if (session != exclude) send(session, message);
        }));
    }

    private static void broadcast(String message) {
        sessions.values().forEach(set -> set.forEach(session -> send(session, message)));
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
