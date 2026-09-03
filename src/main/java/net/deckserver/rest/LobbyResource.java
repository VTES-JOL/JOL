package net.deckserver.rest;

import com.google.common.base.Strings;
import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.GameStatusBean;
import net.deckserver.rest.bean.PlayerActivityStatus;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.ws.WebSocketRegistry;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Dedicated, envelope-free reads/writes for the React lobby page. One
 * targeted GET per widget (GameList/GameCreateForm/GameDetail on
 * LobbyPage.tsx) rather than one combined page bean, so registering a deck
 * doesn't force the games list to refetch and vice versa — see
 * AdminPageResource for the same pattern applied to the admin page.
 */
@Path("/lobby")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LobbyResource extends BaseResource {

    /** Unified game list: owner's private games (any status) + public starting games + invited private starting games. */
    @GET
    @Path("player/games")
    public List<GameStatusBean> games() {
        String player = username();
        return JolAdmin.getGameNames().stream()
                .filter(Objects::nonNull)
                .filter(gameName -> JolAdmin.isViewable(gameName, player))
                .filter(gameName ->
                        (JolAdmin.isPrivate(gameName) && player.equals(JolAdmin.getOwner(gameName)))
                        || (JolAdmin.isStarting(gameName) && JolAdmin.isPublic(gameName))
                        || (JolAdmin.isStarting(gameName) && RegistrationService.isInGame(gameName, player)))
                .distinct()
                .map(gameName -> new GameStatusBean(gameName, player))
                .sorted(Comparator.comparing(GameStatusBean::getFormat)
                        .thenComparing(GameStatusBean::getUpdated, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /** Recently-active player names, for the invite-player datalists on GameCreateForm/GameDetail. */
    @GET
    @Path("players")
    public List<String> players() {
        return PlayerActivityStatus.recentlyActiveNames();
    }

    @GET
    @Path("game-formats")
    public List<String> gameFormats() {
        return JolAdmin.getAvailableGameFormats(username()).stream().map(GameFormat::getLabel).toList();
    }

    private void notifyLobby() {
        WebSocketRegistry.notifyInvalidate(List.of("lobby"), clientId());
    }

    @POST
    @Path("player/games")
    public void createGameReact(CreateGameRequest body) {
        String playerName = username();
        if (!Strings.isNullOrEmpty(playerName)) {
            JolAdmin.createGame(body.name(), "PUBLIC".equals(body.publicFlag()), GameFormat.from(body.format()), playerName);
        }
        notifyLobby();
    }

    @POST
    @Path("player/games/{name}/start")
    public void startGameReact(@PathParam("name") String game) {
        String playerName = username();
        if (!GameService.existsGame(game)) {
            throw new NotFoundException("No such game: " + game);
        }
        String owner = JolAdmin.getOwner(game);
        if (!playerName.equals(owner) && !sc.isUserInRole("SUPER_USER")) {
            throw new ForbiddenException("Only the game owner or a super user can start this game");
        }
        if (!JolAdmin.isStarting(game)) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Game is not in starting status").build());
        }
        long registered = RegistrationService.getRegisteredPlayerCount(game);
        if (registered < 2) {
            String reason = registered == 0
                    ? "No one has registered a deck for this game yet."
                    : "Only one player has registered a deck — you need at least two to start.";
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity(reason).build());
        }
        if (!JolAdmin.startGame(game)) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity("Game could not start - one or more registered decks could not be loaded. Affected players should re-register.").build());
        }
        notifyLobby();
    }

    @DELETE
    @Path("player/games/{name}")
    public void closeGameReact(@PathParam("name") String game) {
        String playerName = username();
        if (!GameService.existsGame(game)) {
            throw new NotFoundException("No such game: " + game);
        }
        String owner = JolAdmin.getOwner(game);
        if (!playerName.equals(owner) && !sc.isUserInRole("ADMIN")) {
            throw new ForbiddenException("Only the game owner or an admin can close this game");
        }
        JolAdmin.endGame(game, true);
        notifyLobby();
    }

    @POST
    @Path("player/games/{name}/invite")
    public void invitePlayerReact(@PathParam("name") String game, InviteRequest body) {
        if (!GameService.existsGame(game)) {
            throw new NotFoundException("No such game: " + game);
        }
        String invitee = PlayerService.canonicalize(body.player());
        if (!PlayerService.getPlayers().contains(invitee)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("No such player: " + body.player()).build());
        }
        boolean selfJoiningPublicGame = username().equals(invitee) && JolAdmin.isPublic(game);
        if (!selfJoiningPublicGame) {
            requireOwnerOrAdmin(game);
        }
        RegistrationService.invitePlayer(game, invitee);
        WebSocketRegistry.notifyInvalidate(List.of("main-games"));
        notifyLobby();
    }

    @DELETE
    @Path("player/games/{name}/invite/{player}")
    public void unInvitePlayerReact(@PathParam("name") String game, @PathParam("player") String player) {
        requireOwnerOrAdmin(game);
        JolAdmin.unInvitePlayer(game, PlayerService.canonicalize(player));
        WebSocketRegistry.notifyInvalidate(List.of("main-games"));
        notifyLobby();
    }

    private void requireOwnerOrAdmin(String game) {
        if (!GameService.existsGame(game)) {
            throw new NotFoundException("No such game: " + game);
        }
        String playerName = username();
        String owner = JolAdmin.getOwner(game);
        if (!playerName.equals(owner) && !sc.isUserInRole("ADMIN")) {
            throw new ForbiddenException("Only the game owner or an admin can manage invites for this game");
        }
    }

    @POST
    @Path("player/games/{name}/deck")
    public RegisterDeckResponse registerDeckReact(@PathParam("name") String game, RegisterDeckRequest body) {
        String playerName = username();
        String message = null;
        if (!Strings.isNullOrEmpty(playerName)) {
            message = JolAdmin.registerDeck(game, playerName, body.deckName());
            WebSocketRegistry.notifyInvalidate(List.of("main-games"));
        }
        notifyLobby();
        return new RegisterDeckResponse(message);
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
    public record RegisterDeckResponse(String message) {}
}
