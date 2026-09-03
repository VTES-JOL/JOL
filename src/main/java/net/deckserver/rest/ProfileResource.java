package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.ProfileBean;
import net.deckserver.services.CountryService;
import net.deckserver.services.PlayerService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Dedicated, envelope-free profile reads/writes for the React profile page —
 * same role MainResource plays for the main page. Deliberately separate from
 * UserResource, whose profile/password/preferences/edge-color PUT endpoints
 * are still shared with legacy ds.js (DS.updateProfile/DS.changePassword/...)
 * and return the full UpdateFactory envelope those call sites still expect;
 * these return the updated ProfileBean directly instead, with no shared-code
 * side effects.
 */
@Path("profile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResource extends BaseResource {

    @GET
    public ProfileBean profile() {
        return new ProfileBean(username());
    }

    @GET
    @Path("countries")
    public List<CountryOption> countries() {
        return CountryService.getCountries().stream()
                .map(name -> new CountryOption(CountryService.getCode(name), name))
                .toList();
    }

    @PUT
    public ProfileBean updateProfile(ProfileRequest body) {
        String player = username();
        PlayerService.updateProfile(player, body.email(), body.discordID(), body.veknID(), body.country());
        return profile();
    }

    /**
     * Requires the current password. There is no "forgot password" or e-mail
     * reset flow yet, so re-authenticating here is the only guard against a
     * left-open session being used to lock the owner out.
     */
    @PUT
    @Path("password")
    public Response changePassword(PasswordRequest body) {
        String player = username();
        if (body.currentPassword() == null || !PlayerService.authenticate(player, body.currentPassword())) {
            return Response.status(Response.Status.FORBIDDEN).entity("Current password is incorrect.").build();
        }
        String next = body.newPassword();
        if (next == null || next.length() < 8) {
            return Response.status(Response.Status.BAD_REQUEST).entity("New password must be at least 8 characters.").build();
        }
        if (next.equals(body.currentPassword())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("New password must be different from your current one.").build();
        }
        PlayerService.changePassword(player, next);
        return Response.noContent().build();
    }

    @PUT
    @Path("preferences")
    public ProfileBean setPreferences(PreferencesRequest body) {
        String player = username();
        JolAdmin.setImageTooltipPreference(player, body.imageTooltips());
        JolAdmin.setNotificationPreference(player, body.notificationsEnabled());
        return profile();
    }

    /** Turn-alert master switch, decoupled from the image-tooltip preference. */
    @PUT
    @Path("notifications")
    public ProfileBean setNotifications(NotificationPrefRequest body) {
        JolAdmin.setNotificationPreference(username(), body.enabled());
        return profile();
    }

    @PUT
    @Path("edge-color")
    public ProfileBean setEdgeColor(EdgeColorRequest body) {
        JolAdmin.setEdgeColor(username(), body.color());
        return profile();
    }

    public record CountryOption(String code, String name) {}
    public record ProfileRequest(String email, String discordID, String veknID, String country) {}
    public record PasswordRequest(String currentPassword, String newPassword) {}
    public record PreferencesRequest(boolean imageTooltips, boolean notificationsEnabled) {}
    public record NotificationPrefRequest(boolean enabled) {}
    public record EdgeColorRequest(String color) {}
}
