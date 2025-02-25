package net.deckserver.servlet;

import net.deckserver.dwr.model.JolAdmin;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = (String)req.getSession().getAttribute("meth");
        JolAdmin.INSTANCE.remove(userName);
        req.getSession().removeAttribute("meth");
        resp.sendRedirect("/jol/");
    }
}
