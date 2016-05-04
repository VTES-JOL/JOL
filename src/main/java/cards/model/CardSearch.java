/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package cards.model;

import java.util.Set;

/**
 * @author administrator
 */
public interface CardSearch {

    /**
     * From CardMap
     */
    public String getId(String nm);

    /**
     * From CardMap
     */
    public Set<String> getNames();

    public CardSet getAllCards();

    //   public CardEntry getCardByName(String name);

    public CardEntry getCardById(String id);

    public CardSet searchByClan(CardSet set, String clan);

    public CardSet searchByDiscipline(CardSet set, String disc);

    public CardSet searchByType(CardSet set, String type);

    public CardSet searchByText(CardSet set, String text);

    public CardSet searchByName(CardSet set, String name);

}
