package net.deckserver.ws;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketRegistry {

    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> sessions = new ConcurrentHashMap<>();
    // gameId -> sessions watching that game
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> gameSessions = new ConcurrentHashMap<>();

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

    private static void broadcast(String message) {
        sessions.values().forEach(set -> set.forEach(session -> send(session, message)));
    }

    private static void send(Session session, String message) {
        try {
            if (session.isOpen()) session.getBasicRemote().sendText(message);
        } catch (Exception ignored) {}
    }
}
