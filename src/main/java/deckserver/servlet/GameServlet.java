package deckserver.servlet;

import deckserver.util.AdminFactory;
import deckserver.util.WebParams;
import deckserver.JolAdminFactory;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Joe User
 */
public abstract class GameServlet extends HttpServlet {

    protected JolAdminFactory admin = null;
    private static final Logger logger = getLogger(GameServlet.class);

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

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected abstract void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    private void preprocessRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebParams params = (WebParams) request.getSession(true).getAttribute("wparams");
        if (params == null) {
            params = new WebParams(request);
            request.getSession().setAttribute("wparams", params);
        } else {
            params.setRequest(request);
        }
        try {
            processRequest(params, request, response);
        } catch (Exception e) {
            logger.error("Error processing request {}", e);
            params.addStatusMsg("Server error, please contact server administrator.");
            gotoMain(request, response);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preprocessRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preprocessRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public abstract String getServletInfo();

    protected final void gotoMain(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/main.jsp").forward(request, response);
    }

    final void loginExpired(WebParams params, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        params.addStatusMsg("Login expired, log in again");
        gotoMain(request, response);
    }
}
