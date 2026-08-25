package net.deckserver.servlet;

import net.deckserver.jobs.GameCleanUp;
import net.deckserver.jobs.PublicGameBuilder;
import net.deckserver.jobs.RegistrationReconciliation;
import net.deckserver.jobs.TournamentJob;
import net.deckserver.services.*;
import net.deckserver.ws.JolWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
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

        // Explicit registration rather than relying on @ServerEndpoint annotation
        // scanning to find JolWebSocketEndpoint — observed in prod (official
        // tomcat:9 docker image) to silently register zero endpoints despite the
        // class being present and correctly annotated; root cause in Tomcat's
        // scanner never pinned down, so this doesn't depend on it. WsFilter
        // itself (the piece that actually intercepts Upgrade requests) IS
        // installed correctly by Tomcat's own WsSci regardless of scan results —
        // the real reason handshakes 404'd despite this being registered turned
        // out to be web.xml missing a catch-all "/" servlet mapping (see its
        // comment): with no Wrapper matching, Tomcat returns 404 before the
        // filter chain even runs. If scanning *did* already register this
        // endpoint (e.g. some other environment), addEndpoint throws for the
        // duplicate path — caught and logged rather than failing context startup.
        ServerContainer container = (ServerContainer) sce.getServletContext().getAttribute(ServerContainer.class.getName());
        if (container == null) {
            logger.error("No WebSocket ServerContainer in ServletContext - /ws/updates will not be available");
        } else {
            try {
                container.addEndpoint(JolWebSocketEndpoint.class);
                logger.info("Registered JolWebSocketEndpoint at /ws/updates");
            } catch (DeploymentException e) {
                logger.info("JolWebSocketEndpoint not registered explicitly (likely already registered via annotation scanning): {}", e.getMessage());
            }
        }

        if (PersistedService.isTestMode()) {
            logger.info("Skipping scheduled job registration - test mode enabled");
            return;
        }
        scheduler.scheduleAtFixedRate(new PublicGameBuilder(), 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new GameCleanUp(), 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new TournamentJob(), 0, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(RefreshTokenService::cleanupExpired, 5, 1440, TimeUnit.MINUTES);
        scheduler.schedule(new RegistrationReconciliation(), 0, TimeUnit.SECONDS);
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
        RefreshTokenService.getInstance().shutdown();
        SiteNotesService.getInstance().shutdown();
        SubscriptionService.getInstance().shutdown();
        TournamentService.getInstance().shutdown();

        scheduler.shutdown();

    }
}