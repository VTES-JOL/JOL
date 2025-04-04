package net.deckserver.servlet;

import net.deckserver.dwr.model.JolAdmin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
//        if (System.getenv().getOrDefault("ENABLE_CAPTCHA", "true").equals("true")) {
//            String captchaResponse = req.getParameter("cf-turnstile-response");
//            boolean verify = Recaptcha.verify(captchaResponse);
//            if (!verify) {
//                resp.sendRedirect("/jol/login");
//                return;
//            }
//        }
        boolean authResult = JolAdmin.INSTANCE.authenticate(username, password);
        if (authResult) {
            req.getSession().setAttribute("meth", username);
            resp.sendRedirect("/jol/");
        } else {
            resp.sendRedirect("/jol/login");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
    }
}
