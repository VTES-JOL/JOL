package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.game.ChatData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("/game/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameActionResource extends BaseResource {

    @PathParam("id")
    private String gameId;

    private String gameName() {
        return GameService.getNameByGameId(gameId);
    }

    /**
     * Replaces DS.submitForm() — ds.js/main.jsp are themselves unreachable now
     * (MainServlet forwards every path to /react/index.html unconditionally),
     * so nothing left consumes the old UpdateFactory envelope or its
     * "showStatus" entry; the React game page submits through
     * GameStateResource's dedicated /game/{id}/view/submit instead.
     */
    @POST
    @Path("submit")
    public void submitForm(SubmitRequest body) {
        String player = username();
        GameModel game = getModel();
        boolean isPlaying = game.getPlayers().contains(player);
        boolean canJudge = JolAdmin.isJudge(player) && !game.getPlayers().contains(player);
        if (isPlaying || canJudge) {
            game.submit(player, ne(body.phase()), ne(body.command()), ne(body.chat()), ne(body.ping()));
        }
    }

    /** Replaces DS.endPlayerTurn() — player ends their own turn */
    @POST
    @Path("end-turn")
    public void endPlayerTurn() {
        String player = username();
        if (RegistrationService.getPlayers(gameName()).contains(player)) {
            getModel().endTurn(player);
        }
    }

    /** Replaces DS.endTurn() — admin forces turn end */
    @POST
    @Path("force-end-turn")
    public void forceEndTurn() {
        String player = username();
        boolean isPlaying = RegistrationService.getPlayers(gameName()).contains(player);
        if (!isPlaying && JolAdmin.isAdmin(player)) {
            JolAdmin.endTurn(gameName(), player);
        }
    }

    /** Replaces DS.gameChat() */
    @POST
    @Path("chat")
    public void gameChat(ChatRequest body) {
        String player = username();
        String gameId = JolAdmin.getGameId(gameName());
        if (RegistrationService.isInGame(gameName(), player)) {
            ChatService.sendMessage(gameId, player, body.chat());
        }
    }

    /** Replaces DS.doToggle() */
    @POST
    @Path("toggle/{toggleId}")
    public void doToggle(@PathParam("toggleId") String toggleId) {
        getView(username()).toggleCollapsed(toggleId);
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
            game.updateGlobalNotes(body.notes());
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
            game.updatePrivateNotes(player, body.notes());
            JolAdmin.recordPlayerAccess(player, gameName());
        }
    }

    /** Replaces DS.rollbackGame() — admin only */
    @POST
    @Path("rollback")
    public void rollbackGame(RollbackRequest body) {
        String player = username();
        if (gameName() != null && JolAdmin.isAdmin(player)) {
            String turn = body.turn();
            String[] parts = (turn != null) ? turn.split(" ") : new String[0];
            if (parts.length < 2) {
                throw new WebApplicationException(
                        Response.status(Response.Status.BAD_REQUEST).entity("Invalid turn format").build());
            }
            String turnCode = parts[1].replaceAll("\\.", "-");
            JolAdmin.rollbackGame(gameName(), player, turnCode);
        }
    }

    /** Replaces DS.replacePlayer() — admin only */
    @PUT
    @Path("replace-player")
    public void replacePlayer(ReplacePlayerRequest body) {
        if (JolAdmin.isAdmin(username())) {
            JolAdmin.replacePlayer(gameName(), body.existingPlayer(), body.newPlayer());
        }
    }

    /** Replaces DS.endGame() */
    @DELETE
    public void endGame() {
        String playerName = username();
        boolean isOwner = GameService.get(gameName()).getOwner().equals(playerName);
        boolean isAdmin = JolAdmin.isAdmin(playerName);
        if (isOwner || isAdmin) {
            JolAdmin.endGame(gameName(), true);
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

    private GameView getView(String player) {
        return getModel().getView(player);
    }

    private static String ne(String arg) {
        return "".equals(arg) ? null : arg;
    }

    public record SubmitRequest(String phase, String command, String chat, String ping) {}
    public record ChatRequest(String chat) {}
    public record NotesRequest(String notes) {}
    public record RollbackRequest(String turn) {}
    public record ReplacePlayerRequest(String existingPlayer, String newPlayer) {}
}
