package net.deckserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DiscordService {
    private static final String DISCORD_AUTHORIZATION_HEADER = String.format("Bot %s", System.getenv("DISCORD_BOT_TOKEN"));
    private static final URI DISCORD_PING_CHANNEL_URI = URI.create(String.format("https://discord.com/api/v%s/channels/%s/messages", System.getenv("DISCORD_API_VERSION"), System.getenv("DISCORD_PING_CHANNEL_ID")));
    private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static void pingPlayer(String discordId, String gameName) {
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
        } catch (Exception e) {
            logger.error("Unable to ping player", e);
        }
    }
}
