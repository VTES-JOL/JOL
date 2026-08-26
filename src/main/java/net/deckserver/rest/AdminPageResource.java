package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.GameActivityStatus;
import net.deckserver.rest.bean.PlayerActivityStatus;
import net.deckserver.rest.bean.UserSummaryBean;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.SiteNotesService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dedicated, envelope-free reads/writes for the React admin page — same role
 * ProfileResource plays for the profile page. Deliberately separate from
 * AdminResource (still shared with legacy ds.js, still returns the
 * UpdateFactory envelope via update()) and from GameActionResource's
 * admin-only game actions (rollback/replace-player/force-end-turn), which
 * this duplicates against the same JolAdmin methods rather than depending on
 * those shared endpoints.
 *
 * One targeted GET per admin tool (PlayerRoles/ReplacePlayer/EndTurn/
 * RollbackGame/IdleGames/SiteNotesEditor on AdminPage.tsx) rather than one
 * combined page bean, so editing one tool doesn't force every sibling tool
 * to refetch — see MainResource for the same pattern applied to the
 * dashboard page.
 *
 * AdminCreator/AdminPageBean had no server-side admin check historically
 * (the legacy page was gated only by hiding the nav link) — these endpoints
 * add one, since that's a plain correctness improvement in code being
 * written fresh, not a behavior change to any existing endpoint.
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
    @Path("user-roles")
    public List<UserSummaryBean> userRoles() {
        requireAdmin();
        return PlayerService.getPlayers().stream()
                .sorted()
                .map(UserSummaryBean::new)
                .filter(UserSummaryBean::isSpecialUser)
                .sorted(Comparator.comparing(UserSummaryBean::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @GET
    @Path("substitutes")
    public List<String> substitutes() {
        requireAdmin();
        return PlayerActivityStatus.recentlyActiveNames();
    }

    /** id -> name, for the game-picker dropdowns shared by ReplacePlayer/EndTurn/RollbackGame. */
    @GET
    @Path("games")
    public Map<String, String> games() {
        requireAdmin();
        return activeGameNames().stream()
                .collect(Collectors.toMap(
                        name -> GameService.get(name).getId(),
                        name -> name,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    @GET
    @Path("idle-games")
    public List<GameActivityStatus> idleGames() {
        requireAdmin();
        OffsetDateTime currentMonth = OffsetDateTime.now().minusMonths(1);
        return activeGameNames().stream()
                .map(GameActivityStatus::new)
                .filter(gameActivityStatus -> gameActivityStatus.timestamp().isBefore(currentMonth))
                .sorted(Comparator.comparing(GameActivityStatus::timestamp))
                .toList();
    }

    private List<String> activeGameNames() {
        return JolAdmin.getGameNames().stream()
                .sorted()
                .filter(JolAdmin::isActive)
                .toList();
    }

    @GET
    @Path("site-notes")
    public SiteNotesResponse siteNotes() {
        requireAdmin();
        return new SiteNotesResponse(SiteNotesService.getRawNotes());
    }

    @PUT
    @Path("roles/{name}")
    public void setRole(@PathParam("name") String player, RoleRequest body) {
        requireAdmin();
        JolAdmin.setRole(player, PlayerRole.valueOf(body.role()), body.value());
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
    public record SiteNotesResponse(String notes) {}
    public record RollbackRequest(String turn) {}
    public record ReplacePlayerRequest(String existingPlayer, String newPlayer) {}
}
