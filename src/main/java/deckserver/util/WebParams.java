/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import nbclient.vtesmodel.JolAdminFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Joe User
 */
public class WebParams {

    public int LOG_LEVEL = 2;
    ServletContext context;
    HttpServletRequest request;
    String status = null;
    String player = "";
    String game = "";
    /**
     * Holds value of property cmdIndex.
     */
    private String cmdIndex;

    public WebParams(HttpServletRequest request) {
        setRequest(request);
    }

    public WebParams(ServletContext ctx, HttpServletRequest request, String game, String player) {
        this.context = ctx;
        this.request = request;
        this.player = player;
        this.game = game;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.context = request.getSession().getServletContext();
        this.request = request;
        this.player = (String) request.getSession().getAttribute("meth");
        log();
    }

    public String getDataDir() {
        return context.getInitParameter("directory");
    }

    public String getPrefix() {
        try {
            return context.getInitParameter("prefix");
        } catch (Exception e) {
            e.printStackTrace();
            return "/jol3";
        }
    }

    public String getAdminEmail() {
        return context.getInitParameter("email");
    }

    public String getCardUrl() {
        return "card?";
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPlayer() {
        //     System.out.println("Getting player from " + this);
        //     System.out.println("Request is " + request);
        //     System.out.println("Session is " + request.getSession());
        return player; //(String) request.getSession().getAttribute("meth");
    }

    public boolean isPlayer() {
        String[] p = JolAdminFactory.INSTANCE.getGame(game).getPlayers();
        return Arrays.asList(p).contains(player);
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public boolean isInGame() {
        if (player == null) return false;
        if (player.equals(JolAdminFactory.INSTANCE.getOwner(game))) return true;
        if (JolAdminFactory.INSTANCE.isSuperUser(player)) return true;
        return isPlayer();
    }

    public String getStatusMsg() {
        return status;
    }

    public void resetStatusMessage() {
        status = null;
    }

    public void addStatusMsg(String msg) {
        if (status == null) status = msg;
        else status = status + "<br>" + msg;
    }

    public void log() {
        if (LOG_LEVEL > 0) {
            HttpSession session = request.getSession();
            String meth = session == null ? "someone" : (String) session.getAttribute("meth");
            System.err.println("Request for " + request.getRequestURI() + " by " + meth);
        }
        if (LOG_LEVEL > 2)
            for (Iterator i = request.getParameterMap().keySet().iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                System.err.println("   " + key + "=" + request.getParameter(key));
            }
        else if (LOG_LEVEL > 1) {
            if (request.getParameter("command") != null && request.getParameter("command").length() > 0)
                System.err.println("  " + "command" + "=" + request.getParameter("command"));
        }
    }

    /**
     * Getter for property cmdIndex.
     *
     * @return Value of property cmdIndex.
     */
    public String getCmdIndex() {
        return this.cmdIndex;
    }

    /**
     * Setter for property cmdIndex.
     *
     * @param cmdIndex New value of property cmdIndex.
     */
    public void setCmdIndex(String cmdIndex) {
        this.cmdIndex = cmdIndex;
    }

}
