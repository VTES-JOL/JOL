/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package deckserver.interfaces;

import java.util.Set;

/**
 * @author administrator
 */
public interface CardSearch {

    /**
     * From CardMap
     */
    String getId(String nm);

    /**
     * From CardMap
     */
    Set<String> getNames();

    CardSet getAllCards();

    CardEntry getCardById(String id);

    CardSet searchByClan(CardSet set, String clan);

    CardSet searchByDiscipline(CardSet set, String disc);

    CardSet searchByType(CardSet set, String type);

    CardSet searchByText(CardSet set, String text);

    CardSet searchByName(CardSet set, String name);

}
