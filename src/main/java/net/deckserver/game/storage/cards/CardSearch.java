/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package net.deckserver.game.storage.cards;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author administrator
 */
public class CardSearch {

    private static final Logger logger = getLogger(CardSearch.class);

    private final CardMap map;
    private Map<String, CardEntry> cardTable = null;
    private CardEntry[] cardArr;

    public CardSearch(String cardlist, String cardmap) {
        map = new CardMap(cardmap);
        readCards(cardlist);
    }

    private void readCards(String file) {
        try (StringReader r = new StringReader(file);
             LineNumberReader reader = new LineNumberReader(r)) {
            List<CardEntry> cardEntries = CardEntry.readCards(map, reader);
            cardArr = cardEntries.toArray(new CardEntry[cardEntries.size()]);
            cardTable = new HashMap<>();
            for (CardEntry aCardArr : cardArr) {
                cardTable.put(aCardArr.getCardId(), aCardArr);
            }
        } catch (Exception e) {
            logger.error("Can't load cards db {}", e);
            throw new IllegalStateException("Cannot load cards db");
        }
    }

    public CardEntry[] getAllCards() {
        return cardArr;
    }

    public CardEntry getCardById(String id) {
        return cardTable.get(id);
    }

    public CardEntry[] searchByName(CardEntry[] set, String name) {
        return searchByField(set, "Name:", name);
    }

    public CardEntry[] searchByType(CardEntry[] set, String type) {
        return searchByField(set, "Cardtype:", type);
    }

    public CardEntry[] searchByText(CardEntry[] set, String text) {
        text = text.toLowerCase();
        Vector<CardEntry> v = new Vector<>();
        for (CardEntry anArr : set) {
            String[] cardText = anArr.getFullText();
            for (String aCardText : cardText) {
                if (aCardText.toLowerCase().indexOf(text) > 0) {
                    v.add(anArr);
                    break;
                }
            }
        }
        set = new CardEntry[v.size()];
        v.toArray(set);
        return set;
    }

    public CardEntry[] searchByField(CardEntry[] set, String field, String value) {
        List<CardEntry> v = new ArrayList<>();
        value = value.toLowerCase();
        for (CardEntry anArr : set) {
            String[] text = anArr.getFullText();
            for (String aText : text)
                if (aText.startsWith(field)) {
                    if (aText.toLowerCase().indexOf(value, 6) > 0)
                        v.add(anArr);
                    break;
                }
        }
        set = new CardEntry[v.size()];
        v.toArray(set);
        return set;
    }

    public String getId(String nm) {
        return map.getId(nm);
    }

    public Set<String> getNames() {
        return map.getNames();
    }

}
