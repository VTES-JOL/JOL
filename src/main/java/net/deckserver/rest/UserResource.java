package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.services.PlayerService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

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

    public record ProfileRequest(String email, String discordID, String veknID, String country) {}
    public record PasswordRequest(String newPassword) {}
    public record PreferencesRequest(boolean imageTooltips) {}
    public record EdgeColorRequest(String color) {}
}
