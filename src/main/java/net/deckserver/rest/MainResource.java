package net.deckserver.rest;

import net.deckserver.rest.bean.ChatEntryBean;
import net.deckserver.rest.bean.GamesSummaryBean;
import net.deckserver.services.GlobalChatService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.SiteNotesService;
import net.deckserver.storage.json.system.UserSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Targeted per-widget reads for the React main/dashboard view. Replaces
 * having every widget poll the whole UpdateFactory envelope (poll()/
 * navigate()) just to read the one slice of it it actually needs.
 */
@Path("main")
@Produces(MediaType.APPLICATION_JSON)
public class MainResource extends BaseResource {

    @GET
    @Path("games")
    public GamesSummaryBean games() {
        return new GamesSummaryBean(username());
    }

    @GET
    @Path("online")
    public List<UserSummary> online() {
        return PlayerService.activeUsers();
    }

    @GET
    @Path("notes")
    public NotesResponse notes() {
        return new NotesResponse(SiteNotesService.getNotesHtml());
    }

    private static final int CHAT_HISTORY_LIMIT = 50;

    /**
     * Most recent chat history, unconditionally — independent of this player's
     * read cursor, for populating the widget on first load. Marks that batch
     * as seen so the next chat() call only returns what's genuinely new,
     * rather than re-delivering the same messages.
     */
    @GET
    @Path("chat/history")
    public List<ChatEntryBean> chatHistory() {
        List<ChatEntryBean> recent = GlobalChatService.getRecentChats(CHAT_HISTORY_LIMIT);
        GlobalChatService.markSeen(username(), recent);
        return recent;
    }

    /**
     * Delta since the given cursor (an entry's timestamp from a prior
     * response, or omitted for none) — the client supplies its own cursor
     * explicitly rather than relying on server-tracked read state, so this
     * is a pure function of {@code since} and safe for TanStack Query to
     * call repeatedly (e.g. on WS-triggered refetch) without losing anything.
     * Still calls markSeen so the nav unread-badge (hasUnseenChats())
     * keeps working exactly as before.
     */
    @GET
    @Path("chat")
    public List<ChatEntryBean> chat(@QueryParam("since") String since) {
        List<ChatEntryBean> result = GlobalChatService.getChatsSince(since);
        GlobalChatService.markSeen(username(), result);
        return result;
    }

    public record NotesResponse(String notes) {}
}
