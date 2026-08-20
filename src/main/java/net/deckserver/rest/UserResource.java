package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RefreshTokenService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * Legacy ds.js-facing endpoints only — each PUT here returns the full
 * UpdateFactory envelope (via {@code update()}) that DS.updateProfile/
 * DS.changePassword/... still expect. The React profile page has its own
 * dedicated, envelope-free equivalents in {@link ProfileResource}.
 */
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {

    /** Replaces DS.updateProfile() */
    @PUT
    @Path("profile")
    public Map<String, Object> updateProfile(ProfileRequest body) {
        String player = username();
        PlayerService.updateProfile(player, body.email(), body.discordID(), body.veknID(), body.country());
        return update(player);
    }

    /** Replaces DS.changePassword() */
    @PUT
    @Path("password")
    public Map<String, Object> changePassword(PasswordRequest body) {
        String player = username();
        PlayerService.changePassword(player, body.newPassword());
        return update(player);
    }

    /** Replaces DS.setUserPreferences() */
    @PUT
    @Path("preferences")
    public Map<String, Object> setUserPreferences(PreferencesRequest body) {
        String player = username();
        JolAdmin.setImageTooltipPreference(player, body.imageTooltips());
        JolAdmin.setNotificationPreference(player, body.notificationsEnabled());
        return update(player);
    }

    /** Replaces DS.setEdgeColor() */
    @PUT
    @Path("edge-color")
    public Map<String, Object> setEdgeColor(EdgeColorRequest body) {
        String player = username();
        JolAdmin.setEdgeColor(player, body.color());
        return update(player);
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
    public Map<String, Object> revokeDevice(@PathParam("id") String id) {
        RefreshTokenService.revoke(username(), id);
        return update();
    }

    /** Log out every device at once. */
    @POST
    @Path("logout-all")
    public Map<String, Object> logoutAllDevices() {
        RefreshTokenService.revokeAll(username());
        return update();
    }

    public record DeviceSummary(String id, String deviceLabel, long createdAt, long lastUsedAt) {}
    public record ProfileRequest(String email, String discordID, String veknID, String country) {}
    public record PasswordRequest(String newPassword) {}
    public record PreferencesRequest(boolean imageTooltips, boolean notificationsEnabled) {}
    public record EdgeColorRequest(String color) {}
}
