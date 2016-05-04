/*
 * LoginServlet.java
 *
 * Created on March 25, 2004, 2:32 PM
 */

package deckserver.login;

import deckserver.util.WebParams;
import nbclient.vtesmodel.JolAdminFactory;

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
public class RegisterServlet extends LoginServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7112659521110123596L;

	/** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out= response.getWriter();
        String player = request.getParameter("newplayer");
        String password = request.getParameter("newpassword");
        String email = request.getParameter("email");
        if(player != null && player.length() > 0 && password != null && password.length() > 0 && email != null && email.length() > 0 && JolAdminFactory.INSTANCE.registerPlayer(player,password,email)) {
            request.getSession(true).setAttribute("meth", player);
            doPlayerPage(params,request,response,player);
        } else {
            if(player == null) player = "";
            if(email == null) email="";
            out.println("<form method=post>");
            out.println("Name:<input name=newplayer value=\"" + player + "\"/><br/>");
            out.println("Password:<input type=password name=newpassword /><br/>");
            out.println("Email:<input name=email value=\"" + email + "\"/><br/>");
            out.println("<input type=submit value=Register />");
            out.println("</form>");
        }
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Register on deckserver";
    }
    
}
