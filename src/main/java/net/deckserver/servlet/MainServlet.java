package net.deckserver.servlet;

import net.deckserver.services.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Every top-level view (main/profile/admin/tournamentAdmin/tournament/
// active/lobby/deck/game) has been ported to React (frontend/, bundled into
// the WAR at /react/*) — this servlet no longer branches between the legacy
// JSP shell and React per path, it just gates auth and always forwards to
// React. "/game" is wildcard-mapped ("/game/*") since getServletPath()
// returns just "/game" regardless of which game id follows.
//
// "/login" also lands here (folded in from the former standalone
// LoginServlet) but skips the auth gate: it's the one page an
// unauthenticated visitor must be able to reach, and the actual login/
// register/logout POSTs are REST calls to AuthResource, not form submissions
// this servlet handles.
//
// "/watch" and "/super" used to be mapped here too, but neither corresponds
// to a real nav-linked view (dead aliases predating this migration) — see
// the React migration plan for how each route above was converted.
@WebServlet({"/", "/main.jsp", "/main", "/lobby", "/deck", "/admin", "/game/*",
        "/tournament", "/tournamentAdmin", "/profile", "/active", "/help", "/help/*", "/login"})
public class MainServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/login".equals(req.getServletPath())) {
            req.getRequestDispatcher("/react/index.html").forward(req, resp);
        } else if (AuthService.authenticate(req, resp).isEmpty()) {
            resp.sendRedirect("/jol/login");
        } else {
            req.getRequestDispatcher("/react/index.html").forward(req, resp);
        }
    }
}
