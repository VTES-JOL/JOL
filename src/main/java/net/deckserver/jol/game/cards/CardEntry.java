/*
 * CardEntry.java
 *
 * Created on September 23, 2003, 9:02 PM
 */

package net.deckserver.jol.game.cards;

/**
 * @author administrator
 */
public interface CardEntry {

    String getType();

    String getCardId();

    String getName();

    String getBaseName();

    String[] getFullText();

    String getText();

    String getGroup();

    boolean isCrypt();

}
