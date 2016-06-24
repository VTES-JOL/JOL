package deckserver.servlet;

import deckserver.util.WebParams;
import deckserver.client.MkMessages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Joe User
 */
public class MessageServlet extends GameServlet {

    /**
     *
     */
    private static final long serialVersionUID = -946370326330447038L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String gamename = request.getParameter("game");
        String count = request.getParameter("num");
        if (count == null) count = "0";
        int num = Integer.parseInt(count);
        if (num <= 0) num = 10;
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        MkMessages msg = new MkMessages(gamename);
        msg.writeMessages(out, num);
        out.close();
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Deckserver Message Servlet";
    }

}
