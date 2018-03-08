package net.deckserver.servlet;

import net.deckserver.dwr.bean.AdminBean;
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
        AdminBean.INSTANCE = new AdminBean();
        scheduler.scheduleAtFixedRate(new ChatPersistenceJob(AdminBean.INSTANCE), 1, 5, TimeUnit.MINUTES);
        logger.info("Initialization complete");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Closing Jyhad Online...");
        scheduler.shutdownNow();
    }
}
