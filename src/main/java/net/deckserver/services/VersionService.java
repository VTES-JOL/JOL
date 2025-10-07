package net.deckserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

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
