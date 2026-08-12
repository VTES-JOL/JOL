package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.GameService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PageResource extends BaseResource {

    /** Replaces DS.doPoll() — returns current page update without changing view. */
    @GET
    @Path("poll")
    public Map<String, Object> poll() {
        return update();
    }

    /**
     * Replaces DS.navigate() and DS.init().
     * Handles "g{gameName}" prefix for game navigation, otherwise sets view directly.
     * Also performs init-time setup (resetChats, setPreferences, setEdgeColorPref).
     */
    @POST
    @Path("navigate")
    public Map<String, Object> navigate(NavigateRequest body) {
        String playerName = username();
        PlayerModel player = JolAdmin.getPlayerModel(playerName);
        if (body != null && body.init()) {
            player.resetChats();
        }

        String target = body != null ? body.target() : null;
        if (target != null) {
            if (target.startsWith("g")) {
                String gameName = GameService.getNameByGameId(target.substring(1));
                player.enterGame(gameName);
            } else {
                player.setView(target);
            }
        } else {
            String currentGame = player.getCurrentGame();
            if (currentGame != null) {
                JolAdmin.resetView(playerName, currentGame);
            }
        }

        Map<String, Object> result = update(playerName);
        result.put("setPreferences", JolAdmin.getImageTooltipPreference(playerName));
        result.put("setEdgeColorPref", JolAdmin.getEdgeColor(playerName));
        return result;
    }

    /** Replaces DS.chat() — global chat */
    @POST
    @Path("chat")
    public Map<String, Object> chat(ChatRequest body) {
        String player = username();
        JolAdmin.chat(player, body.text());
        return update(player);
    }

    public record NavigateRequest(String target, boolean init) {}
    public record ChatRequest(String text) {}
}
