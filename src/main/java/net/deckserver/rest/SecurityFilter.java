package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.services.AuthService;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {

    // Must stay reachable without a valid access token: auth/refresh is exactly
    // what a client calls when its token has expired; login/register run before
    // any token exists at all; logout must still clear cookies even if the
    // access token already expired; config carries only non-secret values
    // (base URL, VAPID public key, captcha site key) the login page needs
    // before it has any session.
    private static final Set<String> PUBLIC_PATHS = Set.of("auth/refresh", "auth/login", "auth/register", "auth/logout", "config");

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (PUBLIC_PATHS.contains(uriInfo.getPath())) {
            return;
        }

        Optional<String> authenticated = AuthService.authenticate(request, response);
        if (authenticated.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String username = authenticated.get();
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
}
