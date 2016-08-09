package net.deckserver.jol;

import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import static org.slf4j.LoggerFactory.getLogger;

@WebListener
public class ApplicationServletInitializer implements ServletContextListener {

    private static final Logger logger = getLogger(ApplicationServletInitializer.class);

    private String data = System.getProperty("jol.data");

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Loading Vtes Online Application");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
