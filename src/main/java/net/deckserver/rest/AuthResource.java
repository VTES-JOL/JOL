package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.Recaptcha;
import net.deckserver.services.AuthService;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.cards.SecuredCardLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Login/register/logout, replacing LoginServlet/RegisterServlet/LogoutServlet's
 * doPost handlers now that login.jsp is gone — the React login page (served
 * statically, unauthenticated) calls these directly instead of submitting an
 * HTML form. All three are excluded from SecurityFilter's auth requirement
 * (see PUBLIC_PATHS there): login/register run before any token exists at all;
 * logout must still clear cookies even if the access token already expired.
 */
@Path("/auth")
public class AuthResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @Context
    HttpHeaders headers;

    /** Called by api/client.ts after any API call gets a 401 — silently mints a new access token off the refresh cookie. */
    @POST
    @Path("refresh")
    public Response refresh() {
        AuthService.AuthResult result = AuthService.authenticate(headers);
        Response response = (result.username().isPresent() ? Response.ok() : Response.status(Response.Status.UNAUTHORIZED)).build();
        attachCookies(response, result.cookiesToSet());
        return response;
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        if (!PlayerService.authenticate(request.username(), request.password())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Response response = Response.ok().build();
        attachCookies(response, AuthService.issueTokens(request.username(), request.remember(), headers));
        if (JolAdmin.isPlaytester(request.username())) {
            attachPlaytestAuthCookies(response);
        }
        return response;
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
        Response response = Response.ok().build();
        attachCookies(response, AuthService.issueTokens(request.username(), false, headers));
        return response;
    }

    @POST
    @Path("logout")
    public Response logout() {
        Response response = Response.ok().build();
        attachCookies(response, AuthService.clearAuth(headers));
        return response;
    }

    private boolean captchaEnabled() {
        return System.getenv().getOrDefault("ENABLE_CAPTCHA", "true").equals("true");
    }

    // MultivaluedMap.add is unambiguously additive, unlike ResponseBuilder.header()
    // chaining — mutating the already-built Response's own header map directly
    // lets this compose cleanly with attachPlaytestAuthCookies's raw Set-Cookie
    // strings on the same response.
    private void attachCookies(Response response, List<NewCookie> cookies) {
        cookies.forEach(c -> response.getHeaders().add(HttpHeaders.SET_COOKIE, c));
    }

    private void attachPlaytestAuthCookies(Response response) {
        logger.info("Setting up playtest auth cookies");
        SecuredCardLoader cardLoader = new SecuredCardLoader("/secured/*");
        try {
            boolean devMode = System.getenv().getOrDefault("TYPE", "dev").equals("dev");
            if (!devMode) {
                String additionalSettings = ";HttpOnly; Domain=deckserver.net; Path=/; Secure;";
                CookiesForCustomPolicy cookies = cardLoader.generateCookies();
                response.getHeaders().add(HttpHeaders.SET_COOKIE, cookies.policyHeaderValue() + additionalSettings);
                response.getHeaders().add(HttpHeaders.SET_COOKIE, cookies.signatureHeaderValue() + additionalSettings);
                response.getHeaders().add(HttpHeaders.SET_COOKIE, cookies.keyPairIdHeaderValue() + additionalSettings);
            }
        } catch (Exception e) {
            logger.error("Unable to set playtest auth cookies", e);
        }
    }

    public record LoginRequest(String username, String password, boolean remember) {}

    public record RegisterRequest(String username, String password, String email, String captchaResponse) {}
}
