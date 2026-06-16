package net.deckserver.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Thread-local carrier for the current HTTP request/response pair.
 * Set by REST resource methods before calling UpdateFactory, cleared in finally.
 * Replaces DWR's WebContextFactory thread-local.
 */
public final class RequestContext {

    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> RESPONSE = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(HttpServletRequest req, HttpServletResponse resp) {
        REQUEST.set(req);
        RESPONSE.set(resp);
    }

    public static HttpServletRequest getRequest() {
        return REQUEST.get();
    }

    public static HttpServletResponse getResponse() {
        return RESPONSE.get();
    }

    public static void clear() {
        REQUEST.remove();
        RESPONSE.remove();
    }
}
