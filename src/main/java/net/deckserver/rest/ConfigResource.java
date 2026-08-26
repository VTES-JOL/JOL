package net.deckserver.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Small runtime config the frontend needs but has no other way to learn —
 * same env vars main.jsp/notification.jsp already inline as script globals
 * for legacy pages, exposed here instead since the React entry
 * (frontend/index.html) is a static built file, not JSP-templated. Excluded
 * from SecurityFilter's auth requirement (see PUBLIC_PATHS there) since the
 * login page needs captchaEnabled/captchaSiteKey before it has any session —
 * none of these values are secret (site keys are public by design; only the
 * Turnstile *secret* key, never sent here, stays server-side in Recaptcha).
 */
@Path("config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource extends BaseResource {

    @GET
    public ConfigResponse config() {
        String baseUrl = System.getenv().getOrDefault("BASE_URL", "https://static.dev.deckserver.net");
        String vapidPublicKey = System.getenv("VAPID_PUBLIC_KEY");
        boolean captchaEnabled = System.getenv().getOrDefault("ENABLE_CAPTCHA", "true").equals("true");
        String captchaSiteKey = System.getenv("JOL_RECAPTCHA_KEY");
        return new ConfigResponse(baseUrl, vapidPublicKey, captchaEnabled, captchaSiteKey);
    }

    public record ConfigResponse(String baseUrl, String vapidPublicKey, boolean captchaEnabled, String captchaSiteKey) {}
}
