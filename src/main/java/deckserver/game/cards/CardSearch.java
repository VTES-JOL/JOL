/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package deckserver.game.cards;

import java.util.Set;

/**
 * @author administrator
 */
public interface CardSearch {

    String getId(String nm);

    Set<String> getNames();

    CardEntry[] getAllCards();

    CardEntry getCardById(String id);

    CardEntry[] searchByType(CardEntry[] set, String type);

    CardEntry[] searchByText(CardEntry[] set, String text);

    CardEntry[] searchByName(CardEntry[] set, String name);

    CardEntry[] searchByField(CardEntry[] set, String field, String value);

}
