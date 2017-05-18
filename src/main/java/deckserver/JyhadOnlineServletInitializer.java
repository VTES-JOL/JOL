package deckserver;

import deckserver.dwr.bean.AdminBean;
import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class JyhadOnlineServletInitializer implements ServletContextListener {

    private static final Logger logger = getLogger(JyhadOnlineServletInitializer.class);

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Starting Jyhad Online...");
        logger.info("Initializing deckserver with " + System.getProperty("jol.data"));
        AdminBean.INSTANCE = new AdminBean();
        String environment = System.getProperty("jol.env", "production");
        servletContextEvent.getServletContext().setAttribute("environment", environment);
        scheduler.scheduleAtFixedRate(new CardPersistenceJob(AdminBean.INSTANCE), 10, 60, TimeUnit.SECONDS);
        logger.info("Initialization complete");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Closing Jyhad Online...");
        AdminBean.INSTANCE.persistChats();
        scheduler.shutdownNow();
    }
}
