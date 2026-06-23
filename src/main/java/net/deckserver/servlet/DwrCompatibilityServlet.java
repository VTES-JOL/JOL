package net.deckserver.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Catches any lingering requests to the old DWR endpoint (/jol/dwr/**) that arrive
 * from browser tabs still running the pre-migration client. Returns a DWR-formatted
 * JavaScript response (HTTP 200 so the old DWR engine evaluates it) that forces an
 * immediate hard reload, landing the user on the current login/main page.
 *
 * DWR's plaincall protocol evaluates the response body as JavaScript, so injecting
 * window.location.reload(true) here is the only reliable way to trigger a refresh —
 * a 4xx/5xx status code does not fire the old DWR errorHandler.
 */
@WebServlet("/dwr/*")
public class DwrCompatibilityServlet extends HttpServlet {

    private static final String RELOAD_RESPONSE =
            "//#DWR-INSERT\n//#DWR-REPLY\nwindow.location.reload(true);\n";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().write(RELOAD_RESPONSE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
