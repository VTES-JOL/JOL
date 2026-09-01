package net.deckserver.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import net.deckserver.push.Subscription;
import net.deckserver.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        SubscriptionService.addSubscription(playerName, subscription);
        logger.info("Successfully registered subscription for {}", playerName);
        return Response.ok(Map.of("status", "ok")).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeSubscription(Map<String, String> body) {
        String playerName = getPlayerName();
        String endpoint = body.get("endpoint");
        if (endpoint == null || endpoint.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing endpoint"))
                    .build();
        }
        SubscriptionService.removeSubscription(playerName, endpoint);
        logger.info("Removed subscription for {}", playerName);
        return Response.ok(Map.of("status", "ok")).build();
    }

    private String getPlayerName() {
        String playerName = securityContext.getUserPrincipal().getName();
        return Optional.ofNullable(playerName).orElseThrow(() -> new NotAuthorizedException("Not authorized"));
    }
}
