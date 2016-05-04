/*
 * DServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.servlet;

import nbclient.model.GameAction;
import nbclient.vtesmodel.JolAdminFactory;
import nbclient.vtesmodel.JolGame;

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
public class TurnServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 659434828298297350L;

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
        String id = request.getQueryString().replaceAll("%20", " ");
//        String cmd = request.getParameter("command");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String gameName = id.substring(0, id.indexOf("-"));
        String turn = id.substring(id.indexOf("-") + 1);
        JolGame game = JolAdminFactory.INSTANCE.getGameFromId(gameName);
        GameAction[] actions = game.getActions(turn);
        out.print(game.getName() + " - " + turn);
        out.print("<hr/>");
        // out.println("<table>");
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].isCommand()) out.print("<b>");
            out.println(actions[i].getText());
            if (actions[i].isCommand()) out.print("</b>");
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
