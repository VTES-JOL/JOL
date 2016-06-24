/*
 * CardEntry.java
 *
 * Created on September 23, 2003, 9:02 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface CardEntry {

    String VAMPIRE = "Vampire";
    String IMBUED = "Imbued";
    String MASTER = "Master";
    String ACTION = "Action";
    String MODIFIER = "Action Modifier";
    String REACTION = "Reaction";
    String COMBAT = "Combat";
    String ALLY = "Ally";
    String RETAINER = "Retainer";
    String POLITICAL = "Political Action";
    String EQUIPMENT = "Equipment";
    String EVENT = "Event";
    String[] types = {VAMPIRE, MASTER, ACTION, MODIFIER, REACTION,
            COMBAT, ALLY, RETAINER, POLITICAL, EQUIPMENT,
            EVENT, IMBUED};

    String getType();

    String getCardId();

    String getName();

    String getBaseName();

    String getShortDescription();

    String[] getFullText();

    String getGroup();

    boolean isAdvanced();

    boolean isCrypt();

}
