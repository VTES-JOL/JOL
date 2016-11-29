package deckserver;

import deckserver.dwr.bean.AdminBean;
import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.slf4j.LoggerFactory.getLogger;

public class JyhadOnlineServletInitializer implements ServletContextListener {

    private static final Logger logger = getLogger(JyhadOnlineServletInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Starting Jyhad Online...");
        logger.info("Initializing deckserver with " + System.getProperty("jol.data"));
        AdminBean.INSTANCE = new AdminBean();
        String environment = System.getProperty("jol.env", "production");
        servletContextEvent.getServletContext().setAttribute("environment", environment);
        logger.info("Initialization complete");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Closing Jyhad Online...");
    }
}
