package net.deckserver.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.ChatEntryBean;
import net.deckserver.rest.bean.NavBean;
import net.deckserver.services.GlobalChatService;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PageResource extends BaseResource {

    /** Targeted read for the top bar (used on every page) — replaces polling the whole envelope for it. */
    @GET
    @Path("nav")
    public NavBean nav() {
        String playerName = username();
        JolAdmin.recordPlayerAccess(playerName);
        return new NavBean(playerName);
    }

    /**
     * Replaces DS.chat() — global chat. Returns the sender's own chat delta
     * directly (same GlobalChatService read cursor MainResource#chat() reads)
     * rather than the whole page envelope, so the caller doesn't need a
     * second round trip to see the message it just sent.
     */
    @POST
    @Path("chat")
    public List<ChatEntryBean> chat(ChatRequest body) {
        String player = username();
        JolAdmin.chat(player, body.text(), clientId());
        return GlobalChatService.getUnseenChats(player);
    }

    public record ChatRequest(String text) {}
}
