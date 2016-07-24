/*
 * CardUtil.java
 *
 * Created on September 24, 2003, 2:14 PM
 */

package deckserver.game.cards;

import net.deckserver.jol.game.cards.CardEntry;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author administrator
 */
public class CardUtil {

    private static final Logger logger = getLogger(CardUtil.class);

    public static OldCardSearch createSearch(String textfile, String mapfile) {
        return new SearchImplOld(textfile, mapfile);
    }

    private static CardSet searchByField(CardSet set, String field, String value) {
        Vector<CardEntry> v = new Vector<>();
        value = value.toLowerCase();
        CardEntry[] arr = set.getCardArray();
        for (CardEntry anArr : arr) {
            String[] text = anArr.getFullText();
            for (String aText : text)
                if (aText.startsWith(field)) {
                    if (aText.toLowerCase().indexOf(value, 6) > 0)
                        v.add(anArr);
                    break;
                }
        }
        arr = new CardEntry[v.size()];
        v.toArray(arr);
        return new SetImpl(arr);
    }

    private static class SearchImplOld implements OldCardSearch {

        final CardMap map;
        private Map<String, CardEntry> cardTable = null;
        private CardEntry[] cardArr;

        SearchImplOld(String cardlist, String cardmap) {
            map = new CardMap(cardmap);
            readCards(cardlist);
        }

        private void readCards(String file) {
            InputStream in = null;
            try (StringReader r = new StringReader(file);
                 LineNumberReader reader = new LineNumberReader(r)) {
                List<CardEntry> cardEntries = CardEntryDetail.readCards(map, reader);
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

        public CardSet getAllCards() {
            return new SetImpl(cardArr);
        }

        public CardEntry getCardById(String id) {
            return cardTable.get(id);
        }

        public CardSet searchByName(CardSet set, String name) {
            return searchByField(set, "Name:", name);
        }

        public CardSet searchByType(CardSet set, String type) {
            return searchByField(set, "Cardtype:", type);
        }

        public CardSet searchByText(CardSet set, String text) {
            CardEntry[] arr = set.getCardArray();
            text = text.toLowerCase();
            Vector<CardEntry> v = new Vector<>();
            for (CardEntry anArr : arr) {
                String[] cardText = anArr.getFullText();
                for (String aCardText : cardText) {
                    if (aCardText.toLowerCase().indexOf(text) > 0) {
                        v.add(anArr);
                        break;
                    }
                }
            }
            arr = new CardEntry[v.size()];
            v.toArray(arr);
            return new SetImpl(arr);
        }

        public String getId(String nm) {
            return map.getId(nm);
        }

        public Set<String> getNames() {
            return map.getNames();
        }
    }

    static class SetImpl implements CardSet {

        CardEntry[] arr;

        SetImpl(CardEntry[] arr) {
            this.arr = arr;
        }

        public CardEntry[] getCardArray() {
            return arr;
        }

    }
}
