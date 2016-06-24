/*
 * Game.java
 *
 * Created on September 19, 2003, 8:10 PM
 */

package deckserver.interfaces;

/**
 * @author administrator
 */
public interface SCard extends SCardContainer {

    String getName();

    SCardContainer getParent();

    String getId();

    String getCardId();

}
