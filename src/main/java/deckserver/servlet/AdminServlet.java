/*
 * AdminServlet.java
 *
 * Created on March 27, 2004, 12:50 PM
 */

package deckserver.servlet;

import deckserver.interfaces.NormalizeDeck;
import deckserver.cards.NormalizeDeckFactory;
import deckserver.util.WebParams;
import deckserver.JolAdminFactory;
import deckserver.client.InteractiveAdmin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * @author Joe User
 */
public class AdminServlet extends GameServlet {

    /**
     *
     */
    private static final long serialVersionUID = 7254561800882501864L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String player = (String) request.getSession().getAttribute("meth");
        if (!getFactory().existsPlayer(player)) gotoMain(request, response);
        try {
            if (!getFactory().isAdmin(player)) {
                out.println("You aren't permitted on this page.");
            } else {
                String gamename = request.getParameter("gamename");
                if (gamename != null) {
                    if (gamename.equals("admin") || gamename.equals("login") || gamename.equals("player") ||
                            gamename.equals("card") || gamename.equals("showdeck") || gamename.equals("register") ||
                            gamename.equals("msg") || admin.existsGame(gamename) || admin.existsPlayer(gamename)) {
                        out.println("Game name \"" + gamename + "\" not legal.");
                    } else {

                        if (JolAdminFactory.INSTANCE.mkGame(gamename)) {
                            out.println("Game " + gamename + " created.");
                            JolAdminFactory.INSTANCE.setOwner(gamename, player);
                        } else
                            out.println("Error creating game " + gamename);
                    }
                }
                String invitee = request.getParameter("invitee");
                if (invitee != null) {
                    String invitegame = request.getParameter("invitegame");
                    JolAdminFactory.INSTANCE.invitePlayer(invitegame, invitee);
                    out.println("<br>Invited " + invitee + " to " + invitegame + ".<br>");
                }
                String startgame = request.getParameter("startgame");
                if (startgame != null) {
                    admin.startGame(startgame); // PENDING allowed to?
                    out.println("<br>Started " + startgame);
                }
                out.println("<form method=post>");
                out.println("Game name: <input name=gamename size=20> <input type=submit value=\"Create Game\">");
                out.println("</form>");
                writeOpenGames(player, out);
                if (JolAdminFactory.INSTANCE.isSuperUser(player)) {
                    String dump = request.getParameter("dump");
                    if (dump != null) {
                        String res = getFactory().dump(dump);
                        params.addStatusMsg(res);
                        out.println("Dump: " + res);
                    }
                    out.println("<form method=post>");
                    out.println("Dump internal structure: <input name=dump />");
                    out.println("</form>");
                    String cmds = request.getParameter("admincmds");
                    if (cmds != null) {
                        String res = InteractiveAdmin.executeBlock(cmds);
                        params.addStatusMsg(res);
                        out.println("Command result : " + res);
                    }
                    out.println("<form method=post>");
                    out.println("Do admin commands: <textarea rows=10 cols=100 name=admincmds></textarea>");
                    out.println("<input type=submit />");
                    out.println("</form>");
                    out.println("Site admin stuff here -pending");
                }
            }
        } finally {
            if (getFactory().isSuperUser(player)) {
                String reset = request.getParameter("reset");
                if (reset != null) JolAdminFactory.INSTANCE = null;
                getFactory();
                out.println("<form method=post>");
                out.println("<input type=submit name=reset value=\"Re-read data\"/>");
                out.println("</form>");
            }
        }
        out.close();
    }

    private void writeOpenGames(String player, PrintWriter out) {
        out.println("<hr>");
        String[] games = JolAdminFactory.INSTANCE.getGames();
        for (int i = 0; i < games.length; i++) {
            try {
                if (JolAdminFactory.INSTANCE.getOwner(games[i]).equals(player) && JolAdminFactory.INSTANCE.isOpen(games[i])) {
                    out.println("Game " + games[i] + " is open for registration.<br>");
                    out.println("Invite a player to submit a deck:");
                    out.println("<form method=post>");
                    out.println("<input type=hidden name=invitegame value=\"" + games[i] + "\"/>");
                    String[] players = JolAdminFactory.INSTANCE.getPlayers();
                    players = (new TreeSet<>(Arrays.asList(players))).toArray(players);
                    out.println("<select name=invitee>");
                    for (int j = 0; j < players.length; j++) {
                        out.println("<option value=\"" + players[j] + "\">" + players[j] + "</option>");
                    }
                    out.println("</select>");
                    out.println("<input type=submit value=\"Invite player\"");
                    out.println("</form>");
                    out.println("<br>");
                    out.println("Players already invited :");
                    String comma = "";
                    for (int j = 0; j < players.length; j++) {
                        if (players[j] == null) continue;
                        if (JolAdminFactory.INSTANCE.isInvited(games[i], players[j])) {
                            out.println(comma + " " + players[j]);
                            comma = ",";
                        } else {
                            String deck = JolAdminFactory.INSTANCE.getGameDeck(games[i], players[j]);
                            if (deck != null) {
                                out.println(comma + " " + players[j]);
                                comma = ",";
                                NormalizeDeck d = NormalizeDeckFactory.getNormalizer(JolAdminFactory.INSTANCE.getCardsForGame(games[i]), deck);
                                int numVamps = d.getCryptSize();
                                int numCards = d.getLibSize();
                                out.println("(C = " + numVamps + ", L = " + numCards + ")");
                            }
                        }
                    }
                    if (comma.length() == 0) out.println("no one");
                    out.println("<br/>");
                    out.println("<form method=post>");
                    out.println("<input type=hidden name=startgame value=\"" + games[i] + "\"/>");
                    out.println("<input type=submit value=\"Start game\"/>");
                    out.println("</form>");
                    out.println("<hr>");
                }
            } catch (Exception e) {
                out.println("(Error with " + games[i] + ")");
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Admin Servlet";
    }

}
