package deckserver;

import deckserver.client.JolAdminFactory;
import deckserver.util.AdminFactory;
import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.slf4j.LoggerFactory.getLogger;

public class JyhadOnlineServletInitializer implements ServletContextListener {

    private static final Logger logger = getLogger(JyhadOnlineServletInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Starting Jyhad Online...");
        AdminFactory.init(servletContextEvent.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Closing Jyhad Online...");
        JolAdminFactory.INSTANCE = null;
    }
}
