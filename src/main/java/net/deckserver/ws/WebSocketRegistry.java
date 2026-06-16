package net.deckserver.ws;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketRegistry {

    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> sessions = new ConcurrentHashMap<>();

    public static void register(String playerName, Session session) {
        sessions.computeIfAbsent(playerName, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public static void unregister(String playerName, Session session) {
        CopyOnWriteArraySet<Session> s = sessions.get(playerName);
        if (s != null) {
            s.remove(session);
            if (s.isEmpty()) sessions.remove(playerName);
        }
    }

    public static void notifyGame(String gameId) {
        broadcast("{\"type\":\"game\",\"id\":\"" + gameId + "\"}");
    }

    public static void notifyMain() {
        broadcast("{\"type\":\"main\"}");
    }

    private static void broadcast(String message) {
        sessions.values().forEach(set -> set.forEach(session -> {
            try {
                if (session.isOpen()) session.getBasicRemote().sendText(message);
            } catch (Exception ignored) {}
        }));
    }
}
