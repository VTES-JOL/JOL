package net.deckserver.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/","/main.jsp"})
public class MainServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("meth") == null) {
            resp.sendRedirect("/jol/login");
        } else {
            req.getRequestDispatcher("/WEB-INF/main.jsp").forward(req, resp);
        }
    }
}
