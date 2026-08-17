package net.deckserver.servlet;

import net.deckserver.JolAdmin;
import net.deckserver.services.AuthService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthService.currentUsername(req).ifPresent(JolAdmin::remove);
        AuthService.clearAuth(req, resp);
        resp.sendRedirect("/jol/");
    }
}
