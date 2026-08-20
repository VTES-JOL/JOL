package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.dwr.bean.NavBean;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.GameService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
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

    /** Targeted read for the top bar (used on every page) — replaces polling the whole envelope for it. */
    @GET
    @Path("nav")
    public NavBean nav() {
        String playerName = username();
        JolAdmin.recordPlayerAccess(playerName);
        return new NavBean(JolAdmin.getPlayerModel(playerName));
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

    /**
     * Replaces DS.chat() — global chat. Returns the sender's own chat delta
     * directly (same PlayerModel.getChat() cursor MainResource#chat() reads)
     * rather than the whole page envelope, so the caller doesn't need a
     * second round trip to see the message it just sent.
     */
    @POST
    @Path("chat")
    public List<ChatEntryBean> chat(ChatRequest body) {
        String player = username();
        JolAdmin.chat(player, body.text(), clientId());
        return JolAdmin.getPlayerModel(player).getChat();
    }

    public record NavigateRequest(String target, boolean init) {}
    public record ChatRequest(String text) {}
}
