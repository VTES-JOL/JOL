package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.push.Subscription;
import net.deckserver.storage.json.system.GameInfo;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.Security;
import java.time.Duration;
import java.util.List;

public class NotificationService {

    static {
        // web-push looks up the "BC" provider by name internally (Cipher/KeyPairGenerator).
        // On a Tomcat redeploy without a JVM restart, a stale provider instance from the
        // previous webapp's classloader can remain registered under the same name, causing
        // "class configured for KeyFactory (provider: BC) cannot be found". Removing any
        // existing registration before re-adding ours ensures it's always this classloader's copy.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String DISCORD_AUTHORIZATION_HEADER = String.format("Bot %s", System.getenv("DISCORD_BOT_TOKEN"));
    private static final URI DISCORD_PING_CHANNEL_URI = URI.create(String.format("https://discord.com/api/v%s/channels/%s/messages", System.getenv("DISCORD_API_VERSION"), System.getenv("DISCORD_PING_CHANNEL_ID")));
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final BouncyCastleProvider BC_PROVIDER = new BouncyCastleProvider();
    private static final KeyPair keyPair = loadKey();

    private record PushPayload(String title, String body, String gameId, String url) {
    }

    public static void pingPlayer(String playerName, String discordId, String gameName) {
        try {
            if (discordId != null && !discordId.isBlank()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(DISCORD_PING_CHANNEL_URI)
                        .header("Content-type", "application/json")
                        .header("Authorization", DISCORD_AUTHORIZATION_HEADER)
                        .timeout(Duration.ofSeconds(10))
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        String.format("{\"content\":\"<@!%s> to %s\"}", discordId, gameName)))
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .handle((response, exception) -> {
                            if (exception == null) {
                                int responseCode = response.statusCode();
                                if (responseCode != 200) {
                                    logger.warn(
                                            "Non-200 response ({}) calling Discord ({}); response body: {}",
                                            responseCode, response.uri(), response.body());
                                }
                            } else logger.error("Error calling Discord", exception);
                            return null;
                        });
            }

            if (!PlayerService.get(playerName).isNotificationsEnabled()) {
                logger.debug("Notifications disabled for player: {}", playerName);
                return;
            }

            List<Subscription> subscriptions = SubscriptionService.getSubscriptions(playerName);
            if (subscriptions.isEmpty()) {
                logger.debug("No subscriptions found for player: {}", playerName);
                return;
            }

            byte[] payload = buildPayload(gameName);
            for (Subscription subscription : subscriptions) {
                try {
                    int statusCode = sendPushMessage(subscription, payload);
                    if (statusCode == 404 || statusCode == 410) {
                        logger.info("Subscription for {} is no longer valid (status {}); removing endpoint {}",
                                playerName, statusCode, subscription.getEndpoint());
                        SubscriptionService.removeSubscription(playerName, subscription.getEndpoint());
                    } else {
                        logger.info("Push notification sent to {} (endpoint {})", playerName, subscription.getEndpoint());
                    }
                } catch (Exception pushException) {
                    logger.error("Failed to send push notification to {} (endpoint {})", playerName, subscription.getEndpoint(), pushException);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to ping player", e);
        }
    }

    private static byte[] buildPayload(String gameName) throws Exception {
        GameInfo gameInfo = GameService.get(gameName);
        String gameId = gameInfo != null ? gameInfo.getId() : null;
        PushPayload payload = new PushPayload(
                "Your Turn",
                String.format("It's your turn to act in %s.", gameName),
                gameId,
                gameId != null ? "/jol/game/" + gameId : "/jol/"
        );
        return objectMapper.writeValueAsBytes(payload);
    }

    public static int sendPushMessage(Subscription sub, byte[] payload) throws Exception {
        logger.info("Attempting to send push notification to endpoint: {}", sub.getEndpoint());

        // Create a notification with the endpoint, userPublicKey from the subscription and a custom payload
        Notification notification = new Notification(
                sub.getEndpoint(),
                sub.getUserPublicKey(),
                sub.getAuthAsBytes(),
                payload,
                86400
        );

        // Instantiate the push service and configure it
        PushService pushService = new PushService();
        pushService.setKeyPair(keyPair);

        // Set the subject - REQUIRED for VAPID
        // Use your website URL or a mailto: URL
        pushService.setSubject("mailto:admin@deckserver.net");

        // Send the notification
        var response = pushService.send(notification);

        int statusCode = response.getStatusLine().getStatusCode();
        logger.info("Push notification sent. Status: {}, Response: {}", statusCode, response);

        if (statusCode != 201 && statusCode != 200 && statusCode != 404 && statusCode != 410) {
            logger.error("Push notification failed with status {}: {}", statusCode, response.getEntity().getContent());
        }

        return statusCode;
    }

    private static KeyPair loadKey() {
        Path keyPath = DataPaths.path("vapid_private.pem");
        try (FileInputStream stream = new FileInputStream(keyPath.toFile()); InputStreamReader reader = new InputStreamReader(stream)) {
            PEMParser pemParser = new PEMParser(reader);
            PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            return new JcaPEMKeyConverter().setProvider(BC_PROVIDER).getKeyPair(pemKeyPair);
        } catch (IOException e) {
            throw new RuntimeException("Could not read private key");
        }
    }
}
