package net.deckserver.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Catches the two calls a pre-React browser tab's ds.js still makes on its own
 * (DS.init/DS.navigate -> POST /navigate, DS.doPoll -> GET /poll) once this
 * deploy has removed the DWR-era view/poll machinery those relied on. Without
 * this, ds.js's errorhandler treats the resulting 404 as a transient
 * "connection lost" and retries forever, so a tab left open across the deploy
 * never recovers on its own.
 *
 * ds.js already special-cases a 401 response as "session expired" and does
 * `location.href = '/jol/login'` (see errorhandler in the old client) —
 * returning 401 here piggybacks on that existing behavior to bounce the
 * stale tab into `/jol/login`, which now serves the current React app.
 */
@Path("/")
public class LegacyClientResource {

    @POST
    @Path("navigate")
    public Response navigate() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("poll")
    public Response poll() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
