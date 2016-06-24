/*
 * PlayerServlet.java
 *
 * Created on March 27, 2004, 4:05 PM
 */

package deckserver.servlet;

import deckserver.interfaces.NormalizeDeck;
import deckserver.cards.NormalizeDeckFactory;
import deckserver.util.WebParams;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Joe User
 */
public class PlayerServlet extends GameServlet {

    /**
     *
     */
    private static final long serialVersionUID = 2273556448003744737L;

    private static final Logger logger = getLogger(PlayerServlet.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String player = (String) request.getSession().getAttribute("meth");
        if (player == null || !admin.existsPlayer(player)) {
            loginExpired(params, request, response);
            return;
        }
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String regdeck = request.getParameter("regdeck");
        String reggame = request.getParameter("reggame");
        String deldeck = request.getParameter("deldeck");
        if (regdeck != null && reggame != null) {
            if (!admin.addPlayerToGame(reggame, player, regdeck))
                out.println("Deck registration failed.<br>");
            else
                out.println("Deck " + regdeck + " registered for " + reggame + ".<br>");
        }
        if (deldeck != null)
            admin.removeDeck(player, deldeck);
        try {
            if (admin.isAdmin(player))
                out.println("<a href=" + params.getPrefix() + "dadmin>Admin page</a><br>");
        } catch (Exception e) {
            logger.error("NPE in admin checking, player is " + player);
        }
        out.println("<form action=" + params.getPrefix() + "login method=post>");
        out.println("<input name=logout value=yes type=hidden />");
        out.println("<input type=submit value=\"Log out\"/>");
        out.println("</form>");
        out.println("<a href=" + params.getPrefix() + "deck>Create</a> a deck.<br>");
        out.println("<a href=" + params.getPrefix() + ">Main page<a>.<br>");
        out.println("<table border=2>");
        String[] games = admin.getGames(player);
        Collection<String> v = new Vector<String>(); // to hold open games
        if (games != null)
            for (int i = 0; i < games.length; i++) {
                if (admin.isOpen(games[i])) v.add(games[i]);
            }
        out.println("<td>");
        out.println("Decks:");
        out.println("<ol>");
        String[] decks = admin.getDeckNames(player);
        if (decks != null) for (int i = 0; i < decks.length; i++) {
            out.println("<li><a href=\"" + params.getPrefix() + "showdeck?deckname=" + decks[i] + "\">" + decks[i] + "</a> ");
            out.println("<a href=\"" + params.getPrefix() + "deck?editdeck=" + decks[i] + "\">Edit</a> ");
            out.println("Delete:<a href=\"" + params.getPrefix() + "player?deldeck=" + decks[i] + "\">yes</a></li>");
        }
        out.println("</ol>");
        out.println("</td>");
        out.println("</table>");
        for (Iterator<String> i = v.iterator(); i.hasNext(); ) {
            String game = i.next();
            String gdeck = admin.getGameDeck(game, player);
            NormalizeDeck nd = NormalizeDeckFactory.getDeckSize(admin.getCardsForGame(game), gdeck);
            out.println("Registration for " + game + " is Crypt: " + nd.getCryptSize() + " Library: " + nd.getLibSize() + "<br />");
        }
        if (v.size() > 0) {
            out.println("Register a deck for a game:");
            out.println("<form method=post>");
            out.println("<select name=reggame>");
            for (Iterator<String> i = v.iterator(); i.hasNext(); ) {
                String game = i.next();
                out.println("<option value=\"" + game + "\"> " + game + "</option>");
            }
            out.println("</select>");
            out.println("<select name=regdeck>");
            for (int i = 0; i < decks.length; i++)
                out.println("<option value=\"" + decks[i] + "\"> " + decks[i] + "</option>");
            out.println("</select>");
            out.println("<input type=submit value=Register />");
            out.println("</form>");
        }
        out.close();
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Player's home page";
    }

}
