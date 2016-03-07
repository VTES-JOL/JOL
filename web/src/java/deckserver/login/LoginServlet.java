/*
 * LoginServlet.java
 *
 * Created on March 25, 2004, 2:32 PM
 */

package deckserver.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import deckserver.servlet.GameServlet;
import deckserver.util.AdminFactory;
import deckserver.util.WebParams;

/**
 *
 * @author  Joe User
 * @version
 */
public class LoginServlet extends GameServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -5147977537689110334L;

	/** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        String player = (String) request.getSession().getAttribute("meth");
        String logout = request.getParameter("logout");
        if(logout != null) {
            player = null;
            request.getSession().removeAttribute("meth");
        }
        if(player == null || player.equals("guest")) {
            String login = request.getParameter("login");
            String pwd = request.getParameter("password");
            if(login == null || pwd == null || !authenticate(login,pwd)) {
                params.addStatusMsg("Login failed");
                doLoginPage(response,login);
    //            request.getSession().setAttribute("meth","guest");
                return;
            }
            request.getSession().setAttribute("meth", login);
            request.getSession().setMaxInactiveInterval(64000);
            player = login;
        }
        doPlayerPage(params,request,response,player);
    }
        
    void doLoginPage(HttpServletResponse response, String login) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        if(login != null) {
            out.println("Failed login for " + login);
            out.println("<hr/>");
        }
        if(login == null) login = "";
        out.println("<form method=post>");
        out.println("Name: <input size=20 name=login value=\"" + login + "\"/><br/>");
        out.println("Password: <input size=20 name=password type=password /><br/>");
        out.println("<input type=submit value=Login />");
        out.println("</form>");
        out.close();
    }
    
    void doPlayerPage(WebParams params, HttpServletRequest request, HttpServletResponse response, String login) throws ServletException, IOException {
        response.sendRedirect(params.getPrefix() + "player");
       //getServletContext().getNamedDispatcher("PlayerServlet").forward(request,response);
/*
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("You're logged in as " + login + ", go ahead and go to your <a href=" + params.getPrefix() + "player>games</a>!");
        out.println("<form method=post>");
        out.println("<input name=logout value=yes type=hidden />");
        out.println("<input type=submit value=\"Log out\"/>");
        out.println("</form>");
        out.close();
 **/
    }
    
    boolean authenticate(String login, String password) {
        return AdminFactory.get(getServletContext()).authenticate(login,password);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Log into deckserver";
    }
    
}
