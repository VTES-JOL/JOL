package net.deckserver.servlet;

import net.deckserver.jobs.GameCleanUp;
import net.deckserver.jobs.PublicGameBuilder;
import net.deckserver.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Listener that handles graceful shutdown of persisted services
 * during web application lifecycle events.
 */
@WebListener
public class JolApplicationInitializer implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(JolApplicationInitializer.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Web application context initialized");
        scheduler.scheduleAtFixedRate(new PublicGameBuilder(), 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new GameCleanUp(), 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Web application context being destroyed - shutting down services");

        RegistrationService.getInstance().shutdown();
        ChatService.getInstance().shutdown();
        DeckService.getInstance().shutdown();
        GameService.getInstance().shutdown();
        GlobalChatService.getInstance().shutdown();
        HistoryService.getInstance().shutdown();
        PlayerActivityService.getInstance().shutdown();
        PlayerGameActivityService.getInstance().shutdown();
        PlayerService.getInstance().shutdown();

        scheduler.shutdown();

    }
}