/*
 * DServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import nbclient.vtesmodel.JolAdminFactory;
import deckserver.util.*;

/**
 *
 * @author  Joe User
 * @version
 */
public abstract class GameServlet extends HttpServlet {
    
    protected JolAdminFactory admin = null;
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        admin = getFactory();
    }
    
    protected JolAdminFactory getFactory() {
        return AdminFactory.get(getServletContext());
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected abstract void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException;
    
    protected void preprocessRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        WebParams params = (WebParams) request.getSession(true).getAttribute("wparams");
        if(params == null) {
            params = new WebParams(request);
       //     System.out.println("Created new WebParams  " + params + " with player " + params.getPlayer());
            request.getSession().setAttribute("wparams", params);
        } else {
            params.setRequest(request);
      //      System.out.println("Using old WebParams " + params + " with player " + params.getPlayer());
        }
        // request.setAttribute("params",params);
        try {
            processRequest(params,request,response);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            params.addStatusMsg("Server error, please contact server administrator.");
            MailUtil.sendError(params,e);
            gotoMain(request,response);
        }
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        preprocessRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        preprocessRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public abstract String getServletInfo();
    
    public final void gotoMain(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        getServletContext().getNamedDispatcher("MainPage").forward(request,response);
    }
    
    public final void loginExpired(WebParams params, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        params.addStatusMsg("Login expired, log in again");
        gotoMain(request,response);
    }
}
