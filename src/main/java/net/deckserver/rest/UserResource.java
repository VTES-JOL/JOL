package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RefreshTokenService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Legacy ds.js-facing endpoints only. These used to return the full
 * UpdateFactory envelope (via {@code update()}) for DS.updateProfile/
 * DS.changePassword/... to feed into processData() — but ds.js/main.jsp are
 * themselves unreachable now (MainServlet forwards every path to
 * /react/index.html unconditionally), so nothing left consumes that
 * envelope; these now just report success. The React profile page has its
 * own dedicated, envelope-free equivalents in {@link ProfileResource}.
 */
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {

    /** Replaces DS.updateProfile() */
    @PUT
    @Path("profile")
    public void updateProfile(ProfileRequest body) {
        String player = username();
        PlayerService.updateProfile(player, body.email(), body.discordID(), body.veknID(), body.country());
    }

    /** Replaces DS.changePassword() */
    @PUT
    @Path("password")
    public void changePassword(PasswordRequest body) {
        PlayerService.changePassword(username(), body.newPassword());
    }

    /** Replaces DS.setUserPreferences() */
    @PUT
    @Path("preferences")
    public void setUserPreferences(PreferencesRequest body) {
        String player = username();
        JolAdmin.setImageTooltipPreference(player, body.imageTooltips());
        JolAdmin.setNotificationPreference(player, body.notificationsEnabled());
    }

    /** Replaces DS.setEdgeColor() */
    @PUT
    @Path("edge-color")
    public void setEdgeColor(EdgeColorRequest body) {
        JolAdmin.setEdgeColor(username(), body.color());
    }

    /** "Remembered" devices for the current user, from the refresh-token store. */
    @GET
    @Path("devices")
    public List<DeviceSummary> listDevices() {
        return RefreshTokenService.list(username()).stream()
                .map(t -> new DeviceSummary(t.getId(), t.getDeviceLabel(), t.getCreatedAt(), t.getLastUsedAt()))
                .toList();
    }

    /** Log out one specific remembered device without affecting others. */
    @DELETE
    @Path("devices/{id}")
    public void revokeDevice(@PathParam("id") String id) {
        RefreshTokenService.revoke(username(), id);
    }

    /** Log out every device at once. */
    @POST
    @Path("logout-all")
    public void logoutAllDevices() {
        RefreshTokenService.revokeAll(username());
    }

    public record DeviceSummary(String id, String deviceLabel, long createdAt, long lastUsedAt) {}
    public record ProfileRequest(String email, String discordID, String veknID, String country) {}
    public record PasswordRequest(String newPassword) {}
    public record PreferencesRequest(boolean imageTooltips, boolean notificationsEnabled) {}
    public record EdgeColorRequest(String color) {}
}
