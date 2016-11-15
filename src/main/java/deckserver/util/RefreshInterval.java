/*
 * CalculateInterval.java
 *
 * Created on April 10, 2005, 10:29 PM
 */

package deckserver.util;

import java.util.Date;

/**
 * @author gfinklan
 */
public final class RefreshInterval {

    public static int calc(Date from) {
        Date to = new Date();
        long interval = to.getTime() - from.getTime();
        if (interval < 10000) return 5000;
        if (interval < 60000) return 10000;
        if (interval < 300000) return 30000;
        if (interval < 6000000) return 120000;
        return 600000;
    }

}
