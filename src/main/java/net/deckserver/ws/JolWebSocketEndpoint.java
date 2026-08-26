package net.deckserver.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.services.AuthService;
import net.deckserver.services.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
            List<String> cookieHeaders = request.getHeaders().getOrDefault("Cookie", List.of());
            Optional<String> username = extractCookie(cookieHeaders, "jol_at").flatMap(AuthService::parseAccessToken);
            // getUserProperties() is one map shared across every handshake for this
            // whole @ServerEndpoint deployment, not a fresh one per connection —
            // confirmed the hard way under Quarkus/Undertow (see
            // quarkus-poc/FINDINGS.md's Phase 3 section): leaving this untouched on
            // a failed handshake let a later unauthenticated connection silently
            // inherit whichever username the *previous successful* handshake left
            // behind, indefinitely, with zero concurrency required to reproduce it.
            // Always writing — never just conditionally on success — closes that.
            if (username.isPresent()) {
                log.debug("WebSocket handshake: authenticated as {}", username.get());
                config.getUserProperties().put(PLAYER_KEY, username.get());
            } else {
                log.warn("WebSocket handshake: no valid access token cookie found");
                // .remove, not .put(key, null) — some Map implementations
                // (this one may be a ConcurrentHashMap under the hood) throw
                // NullPointerException on a null value.
                config.getUserProperties().remove(PLAYER_KEY);
            }
        }

        private static Optional<String> extractCookie(List<String> cookieHeaders, String name) {
            for (String header : cookieHeaders) {
                for (String part : header.split(";")) {
                    String[] kv = part.strip().split("=", 2);
                    if (kv.length == 2 && kv[0].equals(name)) {
                        return Optional.of(kv[1]);
                    }
                }
            }
            return Optional.empty();
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
        // {"type":"hello","clientId":"<uuid>"} tags this session with a per-browser-tab
        // id (generated once in frontend/src/ws/socket.ts) so a REST call from that same
        // tab can ask to be excluded from a broadcast it triggered — see
        // WebSocketRegistry.notifyInvalidate(key, excludeClientId).
        try {
            JsonNode node = MAPPER.readTree(message);
            String type = node.path("type").asText();
            switch (type) {
                case "ping" -> {
                    // "version" lets a pre-React tab still running the old ds.js
                    // frontend (e.g. one open when this backend redeploys) detect
                    // the change and prompt itself to reload — see VersionService.
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
                case "hello" -> {
                    String clientId = node.path("clientId").asText(null);
                    if (clientId != null) WebSocketRegistry.registerClientId(clientId, ws);
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
