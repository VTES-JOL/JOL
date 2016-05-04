/*
 * DeckServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.servlet;

import cards.local.NormalizeDeck;
import cards.local.NormalizeDeckFactory;
import deckserver.util.DeckParams;
import deckserver.util.WebParams;
import nbclient.vtesmodel.JolAdminFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author  Joe User
 * @version
 */
public class ShowDeckServlet extends DeckServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1694299511017661287L;

	/** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String player = (String) request.getSession().getAttribute("meth");
        if(player == null) {
            loginExpired(params,request,response);
        }
        String deckname = request.getParameter("deckname");
        PrintWriter out = response.getWriter();
        try {
            String deck = JolAdminFactory.INSTANCE.getDeck(player,deckname);
            NormalizeDeck nd = NormalizeDeckFactory.getNormalizer(JolAdminFactory.INSTANCE.getBaseCards(),deck);
            DeckParams dp = new DeckParams(null,null,null,null,nd);
            request.setAttribute("dparams", dp);
            RequestDispatcher dispatch = request.getRequestDispatcher("/WEB-INF/jsps/admin/showdeck.jsp");
            dispatch.forward(request,response);
        } catch (Exception e) {
            params.addStatusMsg("Couldn't find deck.");
            response.sendRedirect(params.getPrefix() + "player");
        }
        out.close();
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Read-only deck listing";
    }
    
}
