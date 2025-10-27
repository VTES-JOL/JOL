package net.deckserver.services;

import net.deckserver.push.Subscription;
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
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.Security;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    private static final String DISCORD_AUTHORIZATION_HEADER = String.format("Bot %s", System.getenv("DISCORD_BOT_TOKEN"));
    private static final URI DISCORD_PING_CHANNEL_URI = URI.create(String.format("https://discord.com/api/v%s/channels/%s/messages", System.getenv("DISCORD_API_VERSION"), System.getenv("DISCORD_PING_CHANNEL_ID")));
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Map<String, Subscription> subscriptionMap = new HashMap<>();
    private static final KeyPair keyPair = loadKey();

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
            if (subscriptionMap.containsKey(playerName)) {
                logger.info("Sending message via subscription for player: {}", playerName);
                String message = String.format("It's your turn to act in %s.", gameName);
                Subscription subscription = subscriptionMap.get(playerName);
                try {
                    sendPushMessage(subscription, message.getBytes());
                    logger.info("Push notification sent successfully to {}", playerName);
                } catch (Exception pushException) {
                    logger.error("Failed to send push notification to {}", playerName, pushException);
                }
            } else {
                logger.debug("No subscription found for player: {}", playerName);
            }
        } catch (Exception e) {
            logger.error("Unable to ping player", e);
        }
    }

    public static void registerSubscription(String playerName, Subscription subscription) {
        logger.info("Registering {} for notifications", playerName);
        subscriptionMap.put(playerName, subscription);
    }

    public static void sendPushMessage(Subscription sub, byte[] payload) throws Exception {
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

        if (statusCode != 201 && statusCode != 200) {
            logger.error("Push notification failed with status {}: {}", statusCode, response.getEntity().getContent());
        }
    }

    private static KeyPair loadKey() {
        Security.addProvider(new BouncyCastleProvider());
        Path keyPath = Paths.get(System.getenv("JOL_DATA"), "vapid_private.pem");
        try (FileInputStream stream = new FileInputStream(keyPath.toFile()); InputStreamReader reader = new InputStreamReader(stream)) {
            PEMParser pemParser = new PEMParser(reader);
            PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            return new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
        } catch (IOException e) {
            throw new RuntimeException("Could not read private key");
        }
    }
}
