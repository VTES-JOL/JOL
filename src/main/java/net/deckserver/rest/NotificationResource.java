package net.deckserver.rest;

import net.deckserver.push.Subscription;
import net.deckserver.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Map;
import java.util.Optional;

@Path("/subscription")
public class NotificationResource {

    @Context
    private SecurityContext securityContext;

    private static final Logger logger = LoggerFactory.getLogger(NotificationResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleSubscription(Subscription subscription) {
        String playerName = getPlayerName();
        logger.info("Endpoint: {}", subscription.getEndpoint());
        logger.info("Key: {}", subscription.getKey());
        logger.info("Auth: {}", subscription.getAuth());

        // Validate the subscription data
        if (subscription.getEndpoint() == null || subscription.getEndpoint().isEmpty()) {
            logger.error("Subscription endpoint is null or empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid subscription: missing endpoint"))
                    .build();
        }
        if (subscription.getKey() == null || subscription.getKey().isEmpty()) {
            logger.error("Subscription key is null or empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid subscription: missing key"))
                    .build();
        }
        if (subscription.getAuth() == null || subscription.getAuth().isEmpty()) {
            logger.error("Subscription auth is null or empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid subscription: missing auth"))
                    .build();
        }

        NotificationService.registerSubscription(playerName, subscription);
        logger.info("Successfully registered subscription for {}", playerName);
        return Response.ok(Map.of("status", "ok")).build();
    }

    private String getPlayerName() {
        String playerName = securityContext.getUserPrincipal().getName();
        return Optional.ofNullable(playerName).orElseThrow(() -> new NotAuthorizedException("Not authorized"));
    }
}
