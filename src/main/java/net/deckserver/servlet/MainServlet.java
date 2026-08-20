package net.deckserver.servlet;

import net.deckserver.services.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet({"/", "/main.jsp", "/main", "/lobby", "/deck", "/admin", "/game/*",
        "/tournament", "/tournamentAdmin", "/profile", "/active", "/watch", "/super"})
public class MainServlet extends HttpServlet {

    // Paths whose view has been ported to React (frontend/, bundled into WAR at
    // /react/*). Grows by one entry per route as it's converted — see the React
    // migration plan. Everything else still forwards to the legacy JSP shell.
    private static final Set<String> REACT_ROUTES = Set.of("", "/", "/main", "/main.jsp", "/profile", "/admin", "/tournamentAdmin", "/tournament");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthService.authenticate(req, resp).isEmpty()) {
            resp.sendRedirect("/jol/login");
        } else if (REACT_ROUTES.contains(req.getServletPath())) {
            req.getRequestDispatcher("/react/index.html").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/main.jsp").forward(req, resp);
        }
    }
}
