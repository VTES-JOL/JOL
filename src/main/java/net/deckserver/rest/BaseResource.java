package net.deckserver.rest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;

public abstract class BaseResource {

    @Context
    protected SecurityContext sc;

    @Context
    protected HttpHeaders headers;

    protected String username() {
        return sc.getUserPrincipal().getName();
    }

    /**
     * Per-browser-tab id sent by the frontend on every request (see
     * frontend/src/api/client.ts), correlating this REST call with the same
     * tab's WebSocket session — lets a handler exclude its own caller from a
     * broadcast it triggers. Null for any client that doesn't send it.
     */
    protected String clientId() {
        return headers.getHeaderString("X-Client-Id");
    }
}
