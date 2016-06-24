/*
 * CardMap.java
 *
 * Created on March 3, 2005, 8:00 PM
 */

package cards.local;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author gfinklan
 */
public class CardMap {

    private final Properties map = new Properties();

    private static final Logger logger = getLogger(CardMap.class);

    /**
     * Creates a new instance of CardMap
     */
    public CardMap(String resource) {
        InputStream in = null;
        try {
            StringReader r = new StringReader(resource);
            LineNumberReader reader = new LineNumberReader(r);
            String line = null;
            while ((line = reader.readLine()) != null) {
                int eq = line.indexOf("=");
                String value = line.substring(0, eq);
                String key = line.substring(eq + 1).toLowerCase();
                map.setProperty(key, value);
            }
        } catch (IOException ie) {
            logger.error("Unable to read card map {}", ie);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ie) {
            }
        }
    }

    public String getId(String card) {
        return map.getProperty(card.toLowerCase(), "not found");
    }

    public Set<String> getNames() {
        Set<String> ret = new HashSet<String>();
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            ret.add((String) i.next());
        }
        return ret;
    }
}
