/*
 * LoginServlet.java
 *
 * Created on March 25, 2004, 2:32 PM
 */

package deckserver.dwr;

import deckserver.util.AdminFactory;
import nbclient.vtesmodel.JolAdminFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Joe User
 */
public class Init extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Init.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AdminFactory.init(getServletContext());
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Init deckserver constants.";
    }

    public void destroy() {
        logger.debug("Destroying JOL factory");
        JolAdminFactory.INSTANCE = null;
    }

}
