package net.deckserver.servlet;

import net.deckserver.dwr.model.JolAdmin;
import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

@WebListener
public class JyhadOnlineServletInitializer implements ServletContextListener {

    private static final Logger logger = getLogger(JyhadOnlineServletInitializer.class);

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Starting Jyhad Online...");
        logger.info("Initializing deckserver with " + System.getenv("JOL_DATA"));
        scheduler.scheduleAtFixedRate(new ChatPersistenceJob(), 1, 5, TimeUnit.MINUTES);
        logger.info("Initialization complete");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Closing Jyhad Online...");
        JolAdmin.getInstance().persistChats();
        JolAdmin.getInstance().shutdown();
        scheduler.shutdownNow();
    }
}
