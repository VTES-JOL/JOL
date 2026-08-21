package net.deckserver.rest;

import com.google.common.base.Strings;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.LobbyPageBean;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.ws.WebSocketRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/lobby")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LobbyResource extends BaseResource {

    /** Targeted read for the React lobby page — mirrors LobbyPageBean, standalone. */
    @GET
    @Path("player/games")
    public LobbyPageBean getLobby() {
        return new LobbyPageBean(username());
    }

    /**
     * Dedicated equivalents of the five mutations below, for the React lobby
     * page — same underlying calls, but returning the fresh LobbyPageBean
     * directly instead of the shared UpdateFactory envelope those return for
     * ds.js's DS.createGame/DS.startGame/DS.invitePlayer/DS.unInvitePlayer/
     * DS.registerDeck, which are still in use by the legacy lobby view.
     */
    @POST
    @Path("player/games")
    public LobbyPageBean createGameReact(CreateGameRequest body) {
        String playerName = username();
        if (!Strings.isNullOrEmpty(playerName)) {
            JolAdmin.createGame(body.name(), "PUBLIC".equals(body.publicFlag()), GameFormat.from(body.format()), playerName);
        }
        return getLobby();
    }

    @POST
    @Path("player/games/{name}/start")
    public LobbyPageBean startGameReact(@PathParam("name") String game) {
        String playerName = username();
        if (GameService.existsGame(game)) {
            String owner = JolAdmin.getOwner(game);
            if ((playerName.equals(owner) || JolAdmin.isSuperUser(playerName)) && JolAdmin.isStarting(game)) {
                JolAdmin.startGame(game);
            }
        }
        return getLobby();
    }

    @DELETE
    @Path("player/games/{name}")
    public LobbyPageBean closeGameReact(@PathParam("name") String game) {
        String playerName = username();
        String owner = JolAdmin.getOwner(game);
        if (playerName.equals(owner) || JolAdmin.isAdmin(playerName)) {
            JolAdmin.endGame(game, true);
        }
        return getLobby();
    }

    @POST
    @Path("player/games/{name}/invite")
    public LobbyPageBean invitePlayerReact(@PathParam("name") String game, InviteRequest body) {
        String playerName = username();
        if (playerName != null) {
            RegistrationService.invitePlayer(game, body.player());
            WebSocketRegistry.notifyMain();
            WebSocketRegistry.notifyMainScope("games");
        }
        return getLobby();
    }

    @DELETE
    @Path("player/games/{name}/invite/{player}")
    public LobbyPageBean unInvitePlayerReact(@PathParam("name") String game, @PathParam("player") String player) {
        String playerName = username();
        if (playerName != null) {
            JolAdmin.unInvitePlayer(game, player);
            WebSocketRegistry.notifyMain();
            WebSocketRegistry.notifyMainScope("games");
        }
        return getLobby();
    }

    @POST
    @Path("player/games/{name}/deck")
    public LobbyPageBean registerDeckReact(@PathParam("name") String game, RegisterDeckRequest body) {
        String playerName = username();
        if (!Strings.isNullOrEmpty(playerName)) {
            JolAdmin.registerDeck(game, playerName, body.deckName());
            WebSocketRegistry.notifyMain();
            WebSocketRegistry.notifyMainScope("games");
        }
        return getLobby();
    }

    /** The current player's registered deck for this game — no side effects (unlike DS.loadDeck()). */
    @GET
    @Path("player/games/{name}/deck")
    public Deck getRegisteredDeck(@PathParam("name") String game) {
        return JolAdmin.getGameDeck(game, username());
    }

    // createGame/startGame/invitePlayer/unInvitePlayer/registerDeck (the
    // legacy "games/*" quintet) were ds.js-only and deleted along with
    // ds.js/main.jsp themselves, which were the sole callers — the React
    // lobby page uses the player/games/* equivalents above instead.

    public record CreateGameRequest(String name, String publicFlag, String format) {}
    public record InviteRequest(String player) {}
    public record RegisterDeckRequest(String deckName) {}
}
