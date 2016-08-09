/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import deckserver.client.JolAdmin;
import deckserver.client.JolAdminFactory;
import deckserver.dwr.bean.AdminBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * @author Joe User
 */
public class AdminFactory {

    private static final Logger logger = LoggerFactory.getLogger(AdminFactory.class);

    public static synchronized JolAdminFactory get(ServletContext context) {
        if (JolAdminFactory.INSTANCE == null) try {
            logger.info("Initializing deckserver with " + System.getProperty("jol.data"));
            JolAdminFactory.INSTANCE = new JolAdmin(System.getProperty("jol.data"));
            context.setAttribute("dsadmin", new AdminBean());
            logger.info("Initialization complete");
        } catch (Exception e) {
            logger.error("Error creating admin factory {}", e);
        }
        return JolAdminFactory.INSTANCE;
    }

    public static synchronized AdminBean getBean(ServletContext context) {
        return (AdminBean) context.getAttribute("dsadmin");
    }

    public static synchronized void init(ServletContext context) {
        get(context);
    }

    public static synchronized AdminBean getAdmin() {
        return AdminBean.INSTANCE;
    }
}
