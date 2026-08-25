package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.GameSnapshot;
import net.deckserver.game.model.GameModel;
import net.deckserver.rest.bean.GameSnapshotFactory;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Dedicated, envelope-free reads/writes for the React game page — same role
 * every other *PageResource/*StateResource plays elsewhere. Deliberately
 * separate from GameActionResource (whose ds.js-only submit/end-turn/chat/
 * toggle/rollback/replace-player/end-game methods were deleted outright once
 * ds.js/main.jsp themselves became unreachable) — this resource calls the
 * same GameModel methods directly, then serializes the result through
 * GameSnapshotFactory instead of GameView's now-removed HTML-fragment
 * rendering (see GameView.create()'s removal).
 */
@Path("/game/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameStateResource extends BaseResource {

    @PathParam("id")
    private String gameId;

    private String gameName() {
        return GameService.getNameByGameId(gameId);
    }

    private GameModel getModel() {
        return JolAdmin.getGameModel(gameName());
    }

    @GET
    @Path("view")
    public GameSnapshot getView() {
        return GameSnapshotFactory.build(getModel(), username(), null);
    }

    @POST
    @Path("view/submit")
    public GameSnapshot submit(SubmitRequest body) {
        String player = username();
        GameModel game = getModel();
        boolean isPlaying = game.getPlayers().contains(player);
        boolean canJudge = JolAdmin.isJudge(player) && !isPlaying;
        if (!isPlaying && !canJudge) {
            throw new ForbiddenException("Must be a player in this game or a judge to submit");
        }
        String status = game.submit(player, ne(body.phase()), ne(body.command()), ne(body.chat()), ne(body.ping()), clientId());
        return GameSnapshotFactory.build(game, player, status);
    }

    @POST
    @Path("view/end-turn")
    public GameSnapshot endTurn() {
        String player = username();
        GameModel game = getModel();
        boolean isPlaying = RegistrationService.getPlayers(gameName()).contains(player);
        if (!isPlaying) {
            throw new ForbiddenException("Must be a player in this game to end the turn");
        }
        game.endTurn(player, clientId());
        return GameSnapshotFactory.build(game, player, null);
    }

    private static String ne(String arg) {
        return "".equals(arg) ? null : arg;
    }

    public record SubmitRequest(String phase, String command, String chat, String ping) {}
}
