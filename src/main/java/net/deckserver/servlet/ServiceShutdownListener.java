package net.deckserver.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import net.deckserver.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that handles graceful shutdown of persisted services
 * during web application lifecycle events.
 */
@WebListener
public class ServiceShutdownListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ServiceShutdownListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Web application context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Web application context being destroyed - shutting down services");

        try {
            // Get your service instances and shut them down
            // Adjust this based on how you manage service instances
            RegistrationService.getInstance().shutdown();
            ChatService.getInstance().shutdown();
            DeckService.getInstance().shutdown();
            GameService.getInstance().shutdown();
            GlobalChatService.getInstance().shutdown();
            HistoryService.getInstance().shutdown();
            PlayerActivityService.getInstance().shutdown();
            PlayerGameActivityService.getInstance().shutdown();
            PlayerService.getInstance().shutdown();

            // Add other services here as needed

        } catch (Exception e) {
            logger.error("Error during service shutdown", e);
        }
    }
}