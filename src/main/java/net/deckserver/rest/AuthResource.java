package net.deckserver.rest;

import net.deckserver.services.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {

    @Context
    HttpServletRequest httpRequest;

    @Context
    HttpServletResponse httpResponse;

    /** Called by ds.js after any API call gets a 401 — silently mints a new access token off the refresh cookie. */
    @POST
    @Path("refresh")
    public Response refresh() {
        return AuthService.authenticate(httpRequest, httpResponse)
                .map(username -> Response.ok().build())
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
