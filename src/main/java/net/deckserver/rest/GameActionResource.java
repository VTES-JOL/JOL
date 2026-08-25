package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.game.ChatData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

// The rest of this resource (submit/end-turn/force-end-turn/chat/toggle/
// rollback/replace-player/DELETE) was ds.js-only and deleted along with
// ds.js/main.jsp themselves, which were the sole callers — the React game
// page uses GameStateResource's dedicated /game/{id}/view* equivalents.
// What's left here is genuinely shared with React (deck/players/turns/
// history/notes), already envelope-free.
@Path("/game/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameActionResource extends BaseResource {

    @PathParam("id")
    private String gameId;

    private String gameName() {
        return GameService.getNameByGameId(gameId);
    }

    /** Replaces DS.updateGlobalNotes() */
    @PUT
    @Path("notes/global")
    public void updateGlobalNotes(NotesRequest body) {
        String player = username();
        GameModel game = getModel();
        boolean isPlaying = game.getPlayers().contains(player);
        boolean canJudge = JolAdmin.isJudge(player) && !game.getPlayers().contains(player);
        if (isPlaying || canJudge) {
            game.updateGlobalNotes(body.notes(), clientId());
            JolAdmin.recordPlayerAccess(player, gameName());
        }
    }

    /** Replaces DS.updatePrivateNotes() */
    @PUT
    @Path("notes/private")
    public void updatePrivateNotes(NotesRequest body) {
        String player = username();
        GameModel game = getModel();
        if (game.getPlayers().contains(player)) {
            game.updatePrivateNotes(player, body.notes(), clientId());
            JolAdmin.recordPlayerAccess(player, gameName());
        }
    }

    /** Replaces DS.getGameDeck() */
    @GET
    @Path("deck")
    public Deck getGameDeck() {
        String playerName = username();
        return JolAdmin.getGameDeck(gameName(), playerName);
    }

    /** Replaces DS.getGamePlayers() */
    @GET
    @Path("players")
    public Set<String> getGamePlayers() {
        username(); // auth check
        return RegistrationService.getPlayers(gameName());
    }

    /** Replaces DS.getGameTurns() */
    @GET
    @Path("turns")
    public List<String> getGameTurns() {
        username(); // auth check
        String gameId = JolAdmin.getGameId(gameName());
        return ChatService.getTurns(gameId);
    }

    /** Replaces DS.getHistory() */
    @GET
    @Path("history")
    public List<ChatData> getHistory(@QueryParam("turn") String turn) {
        username(); // auth check
        String gameId = JolAdmin.getGameId(gameName());
        return ChatService.getTurn(gameId, turn);
    }

    private GameModel getModel() {
        return JolAdmin.getGameModel(gameName());
    }

    public record NotesRequest(String notes) {}
}
