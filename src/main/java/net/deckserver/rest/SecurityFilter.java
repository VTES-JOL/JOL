package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.services.AuthService;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Must stay reachable without a valid access token: auth/refresh is exactly
    // what a client calls when its token has expired; login/register run before
    // any token exists at all; logout must still clear cookies even if the
    // access token already expired; config carries only non-secret values
    // (base URL, VAPID public key, captcha site key) the login page needs
    // before it has any session.
    //
    // No leading slash, unlike the Jersey-era version of this set — Quarkus
    // REST's UriInfo.getPath() returns a leading-slash path relative to
    // quarkus.http.root-path (e.g. "/auth/login"), confirmed empirically
    // against a POC spike (see quarkus-poc/FINDINGS.md) before porting this.
    private static final Set<String> PUBLIC_PATHS = Set.of("/auth/refresh", "/auth/login", "/auth/register", "/auth/logout", "/config");

    private static final String PENDING_COOKIES_PROPERTY = "net.deckserver.auth.pendingCookies";

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders headers;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (PUBLIC_PATHS.contains(uriInfo.getPath())) {
            return;
        }

        AuthService.AuthResult result = AuthService.authenticate(headers);
        requestContext.setProperty(PENDING_COOKIES_PROPERTY, result.cookiesToSet());

        if (result.username().isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String username = result.username().get();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String role) {
                return JolAdmin.isInRole(username, role);
            }

            @Override
            public boolean isSecure() {
                return uriInfo.getRequestUri().getScheme().equals("https");
            }

            @Override
            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        });
    }

    /**
     * Attaches any cookies authenticate() decided need setting (a silent
     * access-token refresh, or clearing a stale/invalid refresh cookie) —
     * runs even for a request this same filter aborted with 401, which is
     * exactly what's needed to clear a bad refresh cookie on failure.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        List<NewCookie> cookies = (List<NewCookie>) requestContext.getProperty(PENDING_COOKIES_PROPERTY);
        if (cookies != null) {
            cookies.forEach(cookie -> responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, cookie));
        }
    }
}
