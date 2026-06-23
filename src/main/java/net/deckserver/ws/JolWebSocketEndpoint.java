package net.deckserver.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.services.VersionService;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
            JsonNode node = MAPPER.readTree(message);
            String type = node.path("type").asText();
            switch (type) {
                case "ping" -> {
                    String ver = VersionService.getVersion();
                    String pong = ver != null
                            ? "{\"type\":\"pong\",\"version\":\"" + ver + "\"}"
                            : "{\"type\":\"pong\"}";
                    if (ws.isOpen()) ws.getBasicRemote().sendText(pong);
                }
                case "join" -> {
                    String gameId = node.path("game").asText(null);
                    if (gameId != null) WebSocketRegistry.joinGame(gameId, ws);
                }
                case "leave" -> {
                    String gameId = node.path("game").asText(null);
                    if (gameId != null) WebSocketRegistry.leaveGame(gameId, ws);
                }
                default -> log.debug("WebSocket unknown message type '{}' from session {}", type, ws.getId());
            }
        } catch (Exception e) {
            log.warn("WebSocket onMessage parse error from session {}: {}", ws.getId(), e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session ws, CloseReason reason) {
        String playerName = (String) ws.getUserProperties().get(PLAYER_KEY);
        if (playerName != null) {
            WebSocketRegistry.unregister(playerName, ws);
            CloseReason.CloseCode closeCode = reason == null ? null : reason.getCloseCode();
            String reasonPhrase = reason == null ? "" : reason.getReasonPhrase();
            if (reasonPhrase == null) reasonPhrase = "";
            if (CloseReason.CloseCodes.CLOSED_ABNORMALLY.equals(closeCode)) {
                log.warn("WebSocket closed abnormally for player {} (session {}, code {})", playerName, ws.getId(), closeCode);
            } else {
                log.info("WebSocket closed for player {} (session {}, code {}{})",
                        playerName,
                        ws.getId(),
                        closeCode,
                        reasonPhrase.isBlank() ? "" : ", reason " + reasonPhrase);
            }
        }
    }

    @OnError
    public void onError(Session ws, Throwable t) {
        String playerName = (String) ws.getUserProperties().get(PLAYER_KEY);
        log.error("WebSocket error for player {} (session {}): {}", playerName, ws.getId(), t.getMessage());
        if (playerName != null) WebSocketRegistry.unregister(playerName, ws);
    }
}
