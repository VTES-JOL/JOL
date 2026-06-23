package net.deckserver.rest;

import net.deckserver.dwr.creators.UpdateFactory;
import net.deckserver.servlet.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Map;

public abstract class BaseResource {

    @Context
    protected SecurityContext sc;

    @Context
    protected HttpServletRequest httpRequest;

    @Context
    protected HttpServletResponse httpResponse;

    protected String username() {
        return sc.getUserPrincipal().getName();
    }

    protected Map<String, Object> update(String playerName) {
        RequestContext.set(httpRequest, httpResponse);
        try {
            return UpdateFactory.getUpdate(playerName);
        } finally {
            RequestContext.clear();
        }
    }

    protected Map<String, Object> update() {
        return update(username());
    }
}
