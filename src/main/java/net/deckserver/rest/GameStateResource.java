package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.game.enums.JudgeRequestCategory;
import net.deckserver.rest.bean.GameSnapshot;
import net.deckserver.game.model.GameModel;
import net.deckserver.rest.bean.GameSnapshotFactory;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.JudgeService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.game.JudgeRequestData;
import net.deckserver.ws.WebSocketRegistry;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;

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
        GameModel game = getModel();
        return game.withLock(() -> GameSnapshotFactory.build(game, username(), null));
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
        return game.withLock(() -> {
            String status = game.submit(player, ne(body.phase()), ne(body.command()), ne(body.chat()), ne(body.ping()), clientId());
            return GameSnapshotFactory.build(game, player, status);
        });
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
        return game.withLock(() -> {
            game.endTurn(player, clientId());
            return GameSnapshotFactory.build(game, player, null);
        });
    }

    // ── "Call a judge" ────────────────────────────────────────────────────────
    // One OPEN request per game. Seated players raise / edit / retract it; a
    // judge who is not seated in the game resolves it (and, until tournament→
    // judge assignment exists, only for non-tournament games). Each transition
    // pushes a refresh to the table and to any open judges page. All four
    // return the fresh snapshot so the caller's own game page updates without a
    // second round trip.
    //
    // Only a *resolution* drops a line into game chat (it carries the ruling
    // text, which is worth keeping in the log). Call / edit / retract stay out
    // of chat — the pulsing "Judge Called" button in CommandForm is the live
    // cue, and the Judge page keeps the full request history.

    @POST
    @Path("judge-request")
    public GameSnapshot raiseJudgeRequest(JudgeRequestBody body) {
        String player = username();
        GameModel game = getModel();
        requireSeated(game, player, "Only a player in this game can call a judge");
        String details = requireDetails(body == null ? null : body.details());
        JudgeRequestCategory category = parseCategory(body.category());
        String tournamentName = GameService.get(gameName()).getTournamentName();
        try {
            JudgeService.createRequest(gameId, gameName(), tournamentName, player, category, details);
        } catch (IllegalStateException e) {
            throw new ClientErrorException(e.getMessage(), Response.Status.CONFLICT);
        }
        notifyJudgeChange();
        return snapshot(game, player);
    }

    @PUT
    @Path("judge-request")
    public GameSnapshot editJudgeRequest(JudgeRequestBody body) {
        String player = username();
        GameModel game = getModel();
        JudgeRequestData open = requireOpenRequest();
        if (!open.getRequestedBy().equals(player)) {
            throw new ForbiddenException("Only the player who called the judge can edit the request");
        }
        String details = requireDetails(body == null ? null : body.details());
        JudgeRequestCategory category = parseCategory(body.category());
        if (JudgeService.editRequest(open.getId(), category, details) == null) {
            throw new ClientErrorException("The judge request is no longer open", Response.Status.CONFLICT);
        }
        notifyJudgeChange();
        return snapshot(game, player);
    }

    @POST
    @Path("judge-request/retract")
    public GameSnapshot retractJudgeRequest() {
        String player = username();
        GameModel game = getModel();
        JudgeRequestData open = requireOpenRequest();
        if (!open.getRequestedBy().equals(player)) {
            throw new ForbiddenException("Only the player who called the judge can retract the request");
        }
        if (!JudgeService.retractRequest(open.getId())) {
            throw new ClientErrorException("The judge request is no longer open", Response.Status.CONFLICT);
        }
        notifyJudgeChange();
        return snapshot(game, player);
    }

    @POST
    @Path("judge-request/resolve")
    public GameSnapshot resolveJudgeRequest(ResolveRequest body) {
        String player = username();
        GameModel game = getModel();
        JudgeRequestData open = requireOpenRequest();
        boolean seated = game.getPlayers().contains(player);
        if (!JolAdmin.isJudge(player) || seated) {
            throw new ForbiddenException("Only a judge who is not playing in this game can resolve the request");
        }
        if (open.isTournament()) {
            throw new ForbiddenException("Tournament judge rulings are not available yet");
        }
        JudgeRequestData resolved = JudgeService.resolveRequest(open.getId(), player,
                body == null ? null : body.notes());
        if (resolved == null) {
            throw new ClientErrorException("The judge request is no longer open", Response.Status.CONFLICT);
        }
        String line = "Judge " + player + " resolved the judge request.";
        if (resolved.getResolutionParsed() != null && !resolved.getResolutionParsed().isBlank()) {
            line = "Judge " + player + " resolved the judge request: " + resolved.getResolutionParsed();
        }
        ChatService.sendSystemMessage(gameId, line);
        notifyJudgeChange();
        return snapshot(game, player);
    }

    private void requireSeated(GameModel game, String player, String message) {
        if (!game.getPlayers().contains(player)) {
            throw new ForbiddenException(message);
        }
    }

    private JudgeRequestData requireOpenRequest() {
        JudgeRequestData open = JudgeService.getOpenForGame(gameId);
        if (open == null) {
            throw new NotFoundException("No open judge request for this game");
        }
        return open;
    }

    private static String requireDetails(String details) {
        if (details == null || details.isBlank()) {
            throw new BadRequestException("Request details are required");
        }
        return details;
    }

    private static JudgeRequestCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return JudgeRequestCategory.INCORRECT_PLAY;
        }
        try {
            return JudgeRequestCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown judge request category: " + raw);
        }
    }

    private void notifyJudgeChange() {
        WebSocketRegistry.notifyGame(gameId);
        WebSocketRegistry.notifyInvalidate(List.of("judge", "requests"));
        WebSocketRegistry.notifyInvalidate(List.of("nav")); // refresh the Judges badge count
    }

    private GameSnapshot snapshot(GameModel game, String player) {
        return game.withLock(() -> GameSnapshotFactory.build(game, player, null));
    }

    private static String ne(String arg) {
        return "".equals(arg) ? null : arg;
    }

    public record SubmitRequest(String phase, String command, String chat, String ping) {}

    public record JudgeRequestBody(String category, String details) {}

    public record ResolveRequest(String notes) {}
}
