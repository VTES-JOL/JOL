/*
 * DServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.servlet;

import cards.model.CardEntry;
import cards.model.CardSearch;
import deckserver.util.AdminFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Joe User
 */
public class CardServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 6413386535172727154L;

    /**
     * Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Destroys the servlet.
     */
    public void destroy() {

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getQueryString();
//        String cmd = request.getParameter("command");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        CardSearch cards = AdminFactory.get(getServletContext()).getAllCards();
        CardEntry card = cards.getCardById(id);
        String[] text = card.getFullText();
        // out.println("<table>");
        for (int i = 0; i < text.length; i++) {
            if (i > 0) out.print("  ");
            out.println(text[i]);
            out.println("<br/>");
        }
        //  out.println("</pre>");
        out.close();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }

}
