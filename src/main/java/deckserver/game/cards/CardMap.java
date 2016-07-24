/*
 * CardMap.java
 *
 * Created on March 3, 2005, 8:00 PM
 */

package deckserver.game.cards;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author gfinklan
 */
class CardMap {

    private final Properties map = new Properties();

    private static final Logger logger = getLogger(CardMap.class);

    CardMap(String resource) {
        try (StringReader r = new StringReader(resource);
             LineNumberReader reader = new LineNumberReader(r)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int eq = line.indexOf("=");
                String value = line.substring(0, eq);
                String key = line.substring(eq + 1).toLowerCase();
                map.setProperty(key, value);
            }
        } catch (IOException ie) {
            logger.error("Unable to read card map {}", ie);
        }
    }

    public String getId(String card) {
        return map.getProperty(card.toLowerCase(), "not found");
    }

    Set<String> getNames() {
        return map.keySet().stream().map(o -> (String) o).collect(Collectors.toSet());
    }
}
