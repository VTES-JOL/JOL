package net.deckserver.rest;

import com.google.common.base.Strings;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.RegistrationService;
import net.deckserver.ws.WebSocketRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/lobby")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LobbyResource extends BaseResource {

    /** Replaces DS.createGame() */
    @POST
    @Path("games")
    public Map<String, Object> createGame(CreateGameRequest body) {
        String playerName = username();
        if (!Strings.isNullOrEmpty(playerName)) {
            JolAdmin.createGame(body.name(), "PUBLIC".equals(body.publicFlag()), GameFormat.from(body.format()), playerName);
        }
        return update(playerName);
    }

    /** Replaces DS.startGame() */
    @POST
    @Path("games/{name}/start")
    public Map<String, Object> startGame(@PathParam("name") String game) {
        String playerName = username();
        if ((JolAdmin.getOwner(game).equals(playerName) || JolAdmin.isSuperUser(playerName)) && JolAdmin.isStarting(game)) {
            JolAdmin.startGame(game);
        }
        return update(playerName);
    }

    /** Replaces DS.invitePlayer() */
    @POST
    @Path("games/{name}/invite")
    public Map<String, Object> invitePlayer(@PathParam("name") String game, InviteRequest body) {
        String playerName = username();
        if (playerName != null) {
            RegistrationService.invitePlayer(game, body.player());
            WebSocketRegistry.notifyMain();
        }
        return update(playerName);
    }

    /** Replaces DS.unInvitePlayer() */
    @DELETE
    @Path("games/{name}/invite/{player}")
    public Map<String, Object> unInvitePlayer(@PathParam("name") String game, @PathParam("player") String player) {
        String playerName = username();
        if (playerName != null) {
            JolAdmin.unInvitePlayer(game, player);
            WebSocketRegistry.notifyMain();
        }
        return update(playerName);
    }

    /** Replaces DS.registerDeck() */
    @POST
    @Path("games/{name}/deck")
    public Map<String, Object> registerDeck(@PathParam("name") String game, RegisterDeckRequest body) {
        String playerName = username();
        if (!Strings.isNullOrEmpty(playerName)) {
            JolAdmin.registerDeck(game, playerName, body.deckName());
            WebSocketRegistry.notifyMain();
        }
        return update(playerName);
    }

    public record CreateGameRequest(String name, String publicFlag, String format) {}
    public record InviteRequest(String player) {}
    public record RegisterDeckRequest(String deckName) {}
}
