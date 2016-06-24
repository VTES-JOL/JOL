/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import deckserver.JolAdminFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author Joe User
 */
public class WebParams {

    private static Logger logger = LoggerFactory.getLogger(WebParams.class);

    private ServletContext context;
    private HttpServletRequest request;
    private String status = null;
    private String player = "";
    private String game = "";
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
        return System.getProperty("JOL_DATA");
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
        return player;
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
        if (logger.isTraceEnabled()) {
            logger.trace("Request map:");
            for (String key : request.getParameterMap().keySet()) {
                logger.trace(key + " -> " + request.getParameter(key));
            }
        }
        if (logger.isDebugEnabled()) {
            String command = request.getParameter("command");
            if (command != null && !command.isEmpty()) {
                logger.debug("Command received: " + command);
            }
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
