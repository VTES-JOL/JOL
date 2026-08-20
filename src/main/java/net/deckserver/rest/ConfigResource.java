package net.deckserver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Small runtime config the frontend needs but has no other way to learn —
 * same env vars main.jsp/notification.jsp already inline as script globals
 * for legacy pages, exposed here instead since the React entry
 * (frontend/index.html) is a static built file, not JSP-templated.
 */
@Path("config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource extends BaseResource {

    @GET
    public ConfigResponse config() {
        String baseUrl = System.getenv().getOrDefault("BASE_URL", "https://static.dev.deckserver.net");
        String vapidPublicKey = System.getenv("VAPID_PUBLIC_KEY");
        return new ConfigResponse(baseUrl, vapidPublicKey);
    }

    public record ConfigResponse(String baseUrl, String vapidPublicKey) {}
}
