package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.AdminPageBean;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.SiteNotesService;
import net.deckserver.storage.json.system.PlayerInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Dedicated, envelope-free reads/writes for the React admin page — same role
 * ProfileResource plays for the profile page. Deliberately separate from
 * AdminResource (still shared with legacy ds.js, still returns the
 * UpdateFactory envelope via update()) and from GameActionResource's
 * admin-only game actions (rollback/replace-player/force-end-turn), which
 * this duplicates against the same JolAdmin methods rather than depending on
 * those shared endpoints.
 *
 * AdminCreator/AdminPageBean has no server-side admin check today (the
 * legacy page is gated only by hiding the nav link) — these endpoints add
 * one, since that's a plain correctness improvement in code being written
 * fresh, not a behavior change to any existing endpoint.
 */
@Path("admin-page")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminPageResource extends BaseResource {

    private void requireAdmin() {
        if (!JolAdmin.isAdmin(username())) {
            throw new ForbiddenException("Admin role required");
        }
    }

    @GET
    public AdminPageBean page() {
        requireAdmin();
        return new AdminPageBean(JolAdmin.getPlayerModel(username()));
    }

    @PUT
    @Path("roles/{name}")
    public AdminPageBean setRole(@PathParam("name") String player, RoleRequest body) {
        requireAdmin();
        PlayerInfo target = PlayerService.get(player);
        JolAdmin.setRole(target, PlayerRole.valueOf(body.role()), body.value());
        return page();
    }

    @PUT
    @Path("site-notes")
    public void setSiteNotes(SiteNotesRequest body) {
        requireAdmin();
        SiteNotesService.setNotes(body.notes());
    }

    @DELETE
    @Path("site-notes")
    public void clearSiteNotes() {
        requireAdmin();
        SiteNotesService.clear();
    }

    @POST
    @Path("games/{gameId}/end-turn")
    public void endTurn(@PathParam("gameId") String gameId) {
        requireAdmin();
        JolAdmin.endTurn(gameName(gameId), username());
    }

    @POST
    @Path("games/{gameId}/rollback")
    public void rollback(@PathParam("gameId") String gameId, RollbackRequest body) {
        requireAdmin();
        String turn = body.turn();
        String[] parts = (turn != null) ? turn.split(" ") : new String[0];
        if (parts.length < 2) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid turn format").build());
        }
        String turnCode = parts[1].replaceAll("\\.", "-");
        JolAdmin.rollbackGame(gameName(gameId), username(), turnCode);
    }

    @PUT
    @Path("games/{gameId}/replace-player")
    public void replacePlayer(@PathParam("gameId") String gameId, ReplacePlayerRequest body) {
        requireAdmin();
        JolAdmin.replacePlayer(gameName(gameId), body.existingPlayer(), body.newPlayer());
    }

    @DELETE
    @Path("games/{gameId}")
    public void endGame(@PathParam("gameId") String gameId) {
        requireAdmin();
        JolAdmin.endGame(gameName(gameId), true);
    }

    private String gameName(String gameId) {
        return GameService.getNameByGameId(gameId);
    }

    public record RoleRequest(String role, boolean value) {}
    public record SiteNotesRequest(String notes) {}
    public record RollbackRequest(String turn) {}
    public record ReplacePlayerRequest(String existingPlayer, String newPlayer) {}
}
