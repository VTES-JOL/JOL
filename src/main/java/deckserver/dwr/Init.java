/*
 * LoginServlet.java
 *
 * Created on March 25, 2004, 2:32 PM
 */

package deckserver.dwr;

import deckserver.util.AdminFactory;
import nbclient.vtesmodel.JolAdminFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author  Joe User
 * @version
 */
public class Init extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AdminFactory.init(getServletContext());
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Init deckserver constants.";
    }
    
    public void destroy() {
        System.err.println("Destroying JOL factory");
    	JolAdminFactory.INSTANCE = null;
    }
    
}
