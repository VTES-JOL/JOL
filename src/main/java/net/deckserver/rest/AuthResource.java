package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.Recaptcha;
import net.deckserver.services.AuthService;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.cards.SecuredCardLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Login/register/logout, replacing LoginServlet/RegisterServlet/LogoutServlet's
 * doPost handlers now that login.jsp is gone — the React login page (served
 * statically, unauthenticated) calls these directly instead of submitting an
 * HTML form. All three are excluded from SecurityFilter's auth requirement
 * (see PUBLIC_PATHS there): login/register run before any token exists, and
 * logout must still clear cookies even if the access token already expired.
 */
@Path("/auth")
public class AuthResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @Context
    HttpServletRequest httpRequest;

    @Context
    HttpServletResponse httpResponse;

    /** Called by ds.js/api/client.ts after any API call gets a 401 — silently mints a new access token off the refresh cookie. */
    @POST
    @Path("refresh")
    public Response refresh() {
        return AuthService.authenticate(httpRequest, httpResponse)
                .map(username -> Response.ok().build())
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED).build());
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        if (!PlayerService.authenticate(request.username(), request.password())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        AuthService.issueTokens(request.username(), request.remember(), httpRequest, httpResponse);
        if (JolAdmin.isPlaytester(request.username())) {
            setupPlaytestAuth(httpResponse);
        }
        return Response.ok().build();
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegisterRequest request) {
        // ENABLE_CAPTCHA=false (see CLAUDE.md — local dev) means the React page
        // never renders the Turnstile widget, so request.captchaResponse() would
        // always be blank; skip verification to match, rather than always
        // failing registration in dev.
        if (captchaEnabled() && !Recaptcha.verify(request.captchaResponse())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!PlayerService.registerPlayer(request.username(), request.password(), request.email())) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        AuthService.issueTokens(request.username(), false, httpRequest, httpResponse);
        return Response.ok().build();
    }

    @POST
    @Path("logout")
    public Response logout() {
        AuthService.currentUsername(httpRequest).ifPresent(JolAdmin::remove);
        AuthService.clearAuth(httpRequest, httpResponse);
        return Response.ok().build();
    }

    private boolean captchaEnabled() {
        return System.getenv().getOrDefault("ENABLE_CAPTCHA", "true").equals("true");
    }

    private void setupPlaytestAuth(HttpServletResponse response) {
        logger.info("Setting up playtest auth cookies");
        SecuredCardLoader cardLoader = new SecuredCardLoader("/secured/*");
        try {
            boolean devMode = System.getenv().getOrDefault("TYPE", "dev").equals("dev");
            if (!devMode) {
                String additionalSettings = ";HttpOnly; Domain=deckserver.net; Path=/; Secure;";
                CookiesForCustomPolicy cookies = cardLoader.generateCookies();
                response.addHeader("Set-Cookie", cookies.policyHeaderValue() + additionalSettings);
                response.addHeader("Set-Cookie", cookies.signatureHeaderValue() + additionalSettings);
                response.addHeader("Set-Cookie", cookies.keyPairIdHeaderValue() + additionalSettings);
            }
        } catch (Exception e) {
            logger.error("Unable to set playtest auth cookies", e);
        }
    }

    public record LoginRequest(String username, String password, boolean remember) {}

    public record RegisterRequest(String username, String password, String email, String captchaResponse) {}
}
