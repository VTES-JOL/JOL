package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.ProfileBean;
import net.deckserver.services.CountryService;
import net.deckserver.services.PlayerService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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

    @PUT
    @Path("password")
    public void changePassword(PasswordRequest body) {
        PlayerService.changePassword(username(), body.newPassword());
    }

    @PUT
    @Path("preferences")
    public ProfileBean setPreferences(PreferencesRequest body) {
        String player = username();
        JolAdmin.setImageTooltipPreference(player, body.imageTooltips());
        JolAdmin.setNotificationPreference(player, body.notificationsEnabled());
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
    public record PasswordRequest(String newPassword) {}
    public record PreferencesRequest(boolean imageTooltips, boolean notificationsEnabled) {}
    public record EdgeColorRequest(String color) {}
}
