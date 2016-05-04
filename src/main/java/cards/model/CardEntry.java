/*
 * CardEntry.java
 *
 * Created on September 23, 2003, 9:02 PM
 */

package cards.model;

/**
 * @author administrator
 */
public interface CardEntry {

    public static String VAMPIRE = "Vampire";
    public static String IMBUED = "Imbued";
    public static String MASTER = "Master";
    public static String ACTION = "Action";
    public static String MODIFIER = "Action Modifier";
    public static String REACTION = "Reaction";
    public static String COMBAT = "Combat";
    public static String ALLY = "Ally";
    public static String RETAINER = "Retainer";
    public static String POLITICAL = "Political Action";
    public static String EQUIPMENT = "Equipment";
    public static String EVENT = "Event";
    public static String[] types = {VAMPIRE, MASTER, ACTION, MODIFIER, REACTION,
            COMBAT, ALLY, RETAINER, POLITICAL, EQUIPMENT,
            EVENT, IMBUED};

    public String getType();

    public String getCardId();

    public String getName();

    public String getBaseName();

    public String getShortDescription();

    public String[] getFullText();

    public String getGroup();

    public boolean isAdvanced();

    public boolean isCrypt();

}
