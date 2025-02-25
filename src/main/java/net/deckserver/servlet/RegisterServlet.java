package net.deckserver.servlet;

import net.deckserver.Recaptcha;
import net.deckserver.dwr.model.JolAdmin;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String player = request.getParameter("newplayer");
        String email = request.getParameter("newemail");
        String password = request.getParameter("newpassword");
        String captchaResponse = request.getParameter("cf-turnstile-response");
        boolean verify = Recaptcha.verify(captchaResponse);
        if (verify && JolAdmin.INSTANCE.registerPlayer(player, password, email)) {
            request.getSession().setAttribute("meth", player);
        }
        response.sendRedirect("/jol/");
    }
}
