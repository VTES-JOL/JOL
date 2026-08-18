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

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {

    // Must stay unauthenticated: it's exactly what a client calls when its access token
    // has expired, so it authenticates itself via the refresh cookie.
    private static final String REFRESH_PATH = "auth/refresh";

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (REFRESH_PATH.equals(uriInfo.getPath())) {
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
