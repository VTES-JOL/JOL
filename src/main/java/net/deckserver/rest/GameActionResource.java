package net.deckserver.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.deckserver.JolAdmin;
import net.deckserver.game.model.GameModel;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.CommandErrorData;

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
        if (!isPlaying && !canJudge) {
            throw new ForbiddenException("Must be a player in this game or a judge to update notes");
        }
        game.withLock(() -> game.updateGlobalNotes(body.notes(), clientId()));
        JolAdmin.recordPlayerAccess(player, gameName());
    }

    /** Replaces DS.updatePrivateNotes() */
    @PUT
    @Path("notes/private")
    public void updatePrivateNotes(NotesRequest body) {
        String player = username();
        GameModel game = getModel();
        if (!game.getPlayers().contains(player)) {
            throw new ForbiddenException("Must be a player in this game to update notes");
        }
        game.withLock(() -> game.updatePrivateNotes(player, body.notes(), clientId()));
        JolAdmin.recordPlayerAccess(player, gameName());
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
        String player = username();
        String gameId = JolAdmin.getGameId(gameName());
        List<ChatData> lines = ChatService.getTurn(gameId, turn);
        // Command context (ChatData.invocation) is judge-only: a judge watching a
        // game they are not seated in. Everyone else gets copies with it stripped,
        // so nothing sensitive leaves the server regardless of the client.
        boolean canJudge = JolAdmin.isJudge(player)
                && !RegistrationService.getPlayers(gameName()).contains(player);
        if (canJudge) {
            return lines;
        }
        return lines.stream().map(GameActionResource::withoutInvocation).toList();
    }

    /**
     * Failed command attempts for a turn — mistypes / invalid commands that
     * produced no chat. Judge-only (a judge not seated in the game); everyone
     * else gets 403. Surfaced under the game chat "Commands" toggle.
     */
    @GET
    @Path("command-errors")
    public List<CommandErrorData> getCommandErrors(@QueryParam("turn") String turn) {
        String player = username();
        boolean canJudge = JolAdmin.isJudge(player)
                && !RegistrationService.getPlayers(gameName()).contains(player);
        if (!canJudge) {
            throw new ForbiddenException("Command attempts are visible to judges only");
        }
        return ChatService.getFailedCommands(JolAdmin.getGameId(gameName()), turn);
    }

    private static ChatData withoutInvocation(ChatData c) {
        if (c.getInvocation() == null && c.getInvocationBy() == null && c.getInvocationSeq() == null) {
            return c;
        }
        // invocation / invocationBy / invocationSeq are all judge-only — a fresh
        // copy carrying only the non-privileged fields drops every one of them.
        ChatData copy = new ChatData();
        copy.setTimestamp(c.getTimestamp());
        copy.setMessage(c.getMessage());
        copy.setSource(c.getSource());
        copy.setCommand(c.getCommand());
        return copy;
    }

    private GameModel getModel() {
        return JolAdmin.getGameModel(gameName());
    }

    public record NotesRequest(String notes) {}
}
