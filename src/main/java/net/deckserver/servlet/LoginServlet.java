package net.deckserver.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Serves the React login page (frontend/src/pages/LoginPage.tsx) — the
// actual login/register/logout POSTs are REST calls to AuthResource now
// (net.deckserver.rest), not form submissions this servlet handles. No auth
// gate here (unlike MainServlet): this is the one page an unauthenticated
// visitor must be able to reach.
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/react/index.html").forward(req, resp);
    }
}
