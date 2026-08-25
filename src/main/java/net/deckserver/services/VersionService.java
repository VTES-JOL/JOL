package net.deckserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

// Feeds JolWebSocketEndpoint's ping/pong "version" field — the pre-React
// frontend's ds.js still checks this to prompt an in-place-connected browser
// tab to reload when the deployed backend changes (see ds.js's checkVersion),
// which matters during the React migration rollout: any tab still on the old
// jQuery/ds.js frontend when this backend redeploys needs that prompt to ever
// pick up the new React bundle. The React frontend itself doesn't use this —
// it detects staleness via updateCheck.ts polling its own version.json.
public class VersionService {
    private static final Properties properties = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(VersionService.class);

    static {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream("version.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            logger.error("Unable to load version.properties", e);
            properties.setProperty("version", OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        }
    }

    public static String getVersion() {
        return properties.getProperty("version");
    }
}
