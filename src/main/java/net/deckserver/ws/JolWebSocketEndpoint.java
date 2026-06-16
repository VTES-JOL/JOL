package net.deckserver.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;

@ServerEndpoint(value = "/ws/updates", configurator = JolWebSocketEndpoint.Configurator.class)
public class JolWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(JolWebSocketEndpoint.class);
    private static final String PLAYER_KEY = "playerName";

    static {
        log.info("JolWebSocketEndpoint class loaded — endpoint registered at /ws/updates");
    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
            HttpSession httpSession = (HttpSession) request.getHttpSession();
            if (httpSession == null) {
                log.warn("WebSocket handshake: no HTTP session found");
                return;
            }
            Object player = httpSession.getAttribute("meth");
            if (player != null) {
                log.debug("WebSocket handshake: authenticated as {}", player);
                config.getUserProperties().put(PLAYER_KEY, player.toString());
            } else {
                log.warn("WebSocket handshake: session exists but no 'meth' attribute (not logged in)");
            }
        }
    }

    @OnOpen
    public void onOpen(Session ws, EndpointConfig config) throws IOException {
        String playerName = (String) config.getUserProperties().get(PLAYER_KEY);
        if (playerName == null) {
            log.warn("WebSocket onOpen: rejecting unauthenticated connection {}", ws.getId());
            ws.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            return;
        }
        ws.getUserProperties().put(PLAYER_KEY, playerName);
        WebSocketRegistry.register(playerName, ws);
        log.info("WebSocket opened for player {} (session {})", playerName, ws.getId());
    }

    @OnMessage
    public void onMessage(Session ws, String message) {
        // Clients send {"type":"join","game":"<gameId>"} when entering a game page,
        // and {"type":"leave","game":"<gameId>"} when leaving, so the server can
        // target game notifications to only the sessions watching that game.
        try {
            if (message.contains("\"join\"")) {
                String gameId = extractGameId(message);
                if (gameId != null) WebSocketRegistry.joinGame(gameId, ws);
            } else if (message.contains("\"leave\"")) {
                String gameId = extractGameId(message);
                if (gameId != null) WebSocketRegistry.leaveGame(gameId, ws);
            }
        } catch (Exception e) {
            log.warn("WebSocket onMessage parse error: {}", e.getMessage());
        }
    }

    private static String extractGameId(String message) {
        int idx = message.indexOf("\"game\":\"");
        if (idx < 0) return null;
        int start = idx + 8;
        int end = message.indexOf('"', start);
        return end > start ? message.substring(start, end) : null;
    }

    @OnClose
    public void onClose(Session ws, CloseReason reason) {
        String playerName = (String) ws.getUserProperties().get(PLAYER_KEY);
        if (playerName != null) {
            WebSocketRegistry.unregister(playerName, ws);
            log.info("WebSocket closed for player {} — {} {}", playerName, reason.getCloseCode(), reason.getReasonPhrase());
        }
    }

    @OnError
    public void onError(Session ws, Throwable t) {
        String playerName = (String) ws.getUserProperties().get(PLAYER_KEY);
        log.error("WebSocket error for player {} (session {}): {}", playerName, ws.getId(), t.getMessage());
        if (playerName != null) WebSocketRegistry.unregister(playerName, ws);
    }
}
