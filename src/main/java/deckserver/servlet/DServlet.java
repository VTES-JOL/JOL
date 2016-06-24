/*
 * DServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.servlet;

import deckserver.rich.AdminBean;
import deckserver.rich.GameModel;
import deckserver.util.AdminFactory;
import deckserver.util.WebParams;
import nbclient.vtesmodel.JolGame;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joe User
 */
public class DServlet extends GameServlet {

    /**
     *
     */
    private static final long serialVersionUID = -6185778873425685418L;
    private static Map<String, Map<String, String>> commandHistory = new HashMap<String, Map<String, String>>();

    private static synchronized boolean checkRepeat(String player, String game, String hash) {
        Map<String, String> map = commandHistory.get(player);
        if (map == null) {
            map = new HashMap<String, String>(5);
            commandHistory.put(player, map);
        }
        if (hash.equals(map.get(game)))
            return true;
        map.put(game, hash);
        return false;
    }

    private static final String ne(String arg) {
        if ("".equals(arg)) return null;
        return arg;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String gamename = request.getServletPath();
        if (gamename.length() > 0) gamename = gamename.substring(1);
        if (gamename.equals("") || !admin.existsGame(gamename)) {
            gotoMain(request, response);
            return;
        } else {
            JolGame game = admin.getGame(gamename);
            if (game == null) {
                gotoMain(request, response);
                return;
            }
            if (admin.isBetaGame(gamename)) {
                request.setAttribute("gamename", gamename);
                getServletContext().getRequestDispatcher("/WEB-INF/jsps/dwr/game.jsp").forward(request, response);
                out.close();
                return;
            }
            String player = params.getPlayer();
            params.setGame(gamename);
            params.resetStatusMessage();
            // do this here as well as at the end to help perhaps with refresh problems.
            // setting it here means that refresh won't happen again until after the save.
            if (player != null) admin.recordAccess(gamename, player);
            if (params.isInGame()) {
                AdminBean abean = AdminFactory.getBean(request.getSession().getServletContext());
                GameModel model = abean.getGameModel(gamename);
                String cmd = ne(request.getParameter("command"));
                String msg = ne(request.getParameter("message"));
                String note = request.getParameter("notes");
                String global = request.getParameter("global");
                String phase = ne(request.getParameter("phase"));
                String ping = ne(request.getParameter("ping"));
                String newturn = ne(request.getParameter("newturn"));
                boolean repeated = checkRepeat(player, gamename, cmd + msg + note + global);
                if (!repeated) {
                    params.addStatusMsg(model.submit(player, phase, cmd, msg, ping, newturn, global, note));
                }
//                boolean dosave = false;
//                if(!repeated) {
//                    if(ping != null) {
//                        String email = admin.getEmail(ping);
//                        game.setPingTag(ping);
//                        params.addStatusMsg(MailUtil.ping(params,email));
//                        dosave = true;
//                    }
//                    //if(msg != null) msg = "message " + msg;
//                    response.setContentType("text/html");
//                    if(player != null && player.equals(game.getActivePlayer()) && phase != null && !phase.equals(game.getPhase())) {
//                        game.setPhase(phase);
//                        dosave = true;
//                    }
//                    if(player != null && note != null) {
//                        if(note.length() > 8000) note = note.substring(note.length() - 8000);
//                        game.setPlayerText(player,note);
//                        dosave = true;
//                    }
//                    if(player != null && global != null) {
//                        if(global.length() > 800) global = global.substring(0,799);
//                        game.setGlobalText(global);
//                        dosave = true;
//                    }
//                    if(player != null && (cmd != null || msg != null)) {
//                        DoCommand commander = new DoCommand(gamename);
//                        if(cmd != null) {
//                        	StringTokenizer st = new StringTokenizer(cmd,";");
//        					while(st.hasMoreTokens()) {
//        						String command = st.nextToken();
//        						params.addStatusMsg(commander.doCommand(player,tokenize(command)));
//        					}
//                        }
//                        if(msg != null) params.addStatusMsg(commander.doMessage(player,msg));
//                        dosave = true;
//                    }
//                    if(newturn != null && newturn.equals("yes") && player.equals(game.getActivePlayer())) {
//                        try {
//                            MailUtil.sendTurn(game);
//                        } catch (Error e) {
//                            params.addStatusMsg("Turn email failed");
//                        }
//                        game.newTurn();
//                        String email = admin.getEmail(game.getActivePlayer());
//                        MailUtil.ping(params, email);
//                        dosave = true;
//                    }
//                    if(dosave) {
//                        admin.saveGame(game);
//                    }
//                }
            }
            if (player != null) admin.recordAccess(gamename, player);
            request.getSession().setAttribute("wparams", params);
            getServletContext().getRequestDispatcher("/WEB-INF/jsps/topframe/game.jsp").forward(request, response);
            out.close();
        }
    }
    
    /*
    private String[] tokenize(String arg) {
        StringTokenizer tok = new StringTokenizer(arg);
        String[] ret = new String[tok.countTokens() + 3];
        for(int i = 3; i < ret.length; i++)
            ret[i] = tok.nextToken();
        return ret;
    }*/

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Deckserver Servlet";
    }

}
