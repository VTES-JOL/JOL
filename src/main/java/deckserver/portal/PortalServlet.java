/*
 * DServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.portal;

import deckserver.util.AdminFactory;
import nbclient.vtesmodel.JolAdminFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Joe User
 */
public class PortalServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 5530851285720637097L;
    protected JolAdminFactory admin = null;

    /**
     * Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        admin = getFactory();
    }

    protected JolAdminFactory getFactory() {
        return AdminFactory.get(getServletContext());
    }

    /**
     * Destroys the servlet.
     */
    public void destroy() {
    }

    boolean authenticate(String login, String password) {
        return admin.authenticate(login, password);
    }

    private void processLogin(HttpServletRequest request, PortalParams params) {
        String logout = request.getParameter("logout");
        if (logout != null && logout.equals("yes")) {
            params.setPlayer(null);
            request.getSession().removeAttribute("meth");
        }
        String login = request.getParameter("login");
        String pwd = request.getParameter("password");
        if (login != null) {
            if (pwd == null || !authenticate(login, pwd)) {
                params.addStatusMsg("Login failed");
            } else {
                params.setPlayer(login);
                request.getSession().setAttribute("meth", login);
                request.getSession().setMaxInactiveInterval(-1);
            }
        }
    }

    private void processGame(HttpServletRequest request, PortalParams params) {
        String game = request.getPathInfo();
        if (admin.existsGame(game)) params.setGame(game);
        if (game != null) params.addStatusMsg("No such game.");
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PortalParams params = new PortalParams(request);
        request.getSession(true).setAttribute("params", params);
        processLogin(request, params);
        processGame(request, params);
        getServletContext().getNamedDispatcher("PortalPage").forward(request, response);
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
        return "Deckserver Portal Servlet";
    }
    
    /*
    public final void gotoMain(ServletContext context, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        getServletContext().getNamedDispatcher("MainPage").forward(request,response);
    }
     
    public final void loginExpired(WebParams params, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        params.addStatusMsg("Login expired, log in again");
        gotoMain(request,response);
    } */
}
