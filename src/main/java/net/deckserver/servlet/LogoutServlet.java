package net.deckserver.servlet;

import net.deckserver.JolAdmin;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session != null) {
            String userName = (String) session.getAttribute("meth");
            JolAdmin.remove(userName);
            session.invalidate();
        }
        resp.sendRedirect("/jol/");
    }
}
