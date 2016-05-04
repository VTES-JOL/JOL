/*
 * CardUtil.java
 *
 * Created on September 24, 2003, 2:14 PM
 */

package cards.local;

import cards.model.CardEntry;
import cards.model.CardSearch;
import cards.model.CardSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.*;

/**
 *
 * @author  administrator
 */
public class CardUtil {
    
    private static boolean showstatus;
    
    public static CardSearch createSearch(String textfile, String mapfile) {
        return new SearchImpl(textfile,mapfile);
    }
    
    public static CardSearch combineSearch(CardSearch[] search) {
        return new CombSearch(search);
    }
    
    public static void setShow(boolean show) {
        showstatus = show;
    }
    
    private static CardSet searchByField(CardSet set, String field, String value) {
        Vector<CardEntry> v = new Vector<CardEntry>();
        value = value.toLowerCase();
        CardEntry[] arr = set.getCardArray();
        for(int i = 0; i < arr.length; i++) {
            String[] text = arr[i].getFullText();
            boolean notfound = true;
            for(int j = 0; j < text.length; j++)
                if(text[j].startsWith(field)) {
                    //                    System.err.println("Checking " + text[j]);
                    if ((value != null) && text[j].toLowerCase().indexOf(value,6) > 0)
                        v.add(arr[i]);
                    notfound = false;
                    break;
                }
            if((value == null) && notfound) v.add(arr[i]);
        }
        arr = new CardEntry[v.size()];
        v.toArray(arr);
        return new SetImpl(arr);
    }

    static class SearchImpl implements CardSearch {
    
        final CardMap map;
        private Map<String, CardEntry> cardTable = null;
        private CardEntry[] cardArr;
    
        SearchImpl(String cardlist, String cardmap) {
            map = new CardMap(cardmap);
            readCards(cardlist);
        }
        
        private void readCards(String file) {
            InputStream in = null;
            try {
               // in = getClass().getClassLoader().getResourceAsStream(file);
               // byte[] bytes = toByteArray(in);
              //  in = new ByteArrayInputStream(bytes);
                StringReader r = new StringReader(file);
                //InputStreamReader r = new InputStreamReader(in,"ISO-8859-1");
                LineNumberReader reader = new LineNumberReader(r);
                cardArr = TranslateCard.readCards(map,reader);
                cardTable = new HashMap<String, CardEntry>();
                for(int i = 0; i < cardArr.length; i++) {
                    cardTable.put(cardArr[i].getCardId(),cardArr[i]);
                    if(showstatus && cardArr[i].getCardId().equals("not found")) System.out.println(cardArr[i].getCardId() + " = " + cardArr[i].getName());
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new IllegalStateException("Cannot load cards db");
            } finally {
                try {
                    if(in != null) in.close();
                } catch (IOException ie) {
                    
                }
            }
        }
        
        public CardSet getAllCards() {
            return new SetImpl(cardArr);
        }
        
        public CardEntry getCardById(String id) {
            return cardTable.get(id);
        }
        
        /*
        private CardEntry getCardByName(String name) {
            name = name.toLowerCase();
            for(int i = 0; i < cardArr.length; i++)
                if(cardArr[i].getName().toLowerCase().startsWith(name)) {
                    //               System.err.println("Found " + cardArr[i]);
                    return cardArr[i];
                }
            return null;
        } */
        
        public CardSet searchByName(CardSet set, String name) {
            return searchByField(set,"Name:", name);
        }
        
        public CardSet searchByClan(CardSet set, String clan) {
            return searchByField(set, "Clan:", clan);
        }
        
        public CardSet searchByDiscipline(CardSet set, String disc) {
            return searchByField(set, "Discipline:", disc);
        }
        
        public CardSet searchByType(CardSet set, String type) {
            return searchByField(set, "Cardtype:", type);
        }
        
        public CardSet searchByText(CardSet set, String text) {
            CardEntry[] arr = set.getCardArray();
            text = text.toLowerCase();
            Vector<CardEntry> v = new Vector<CardEntry>();
            for(int i = 0; i < arr.length; i++) {
                String[] cardText = arr[i].getFullText();
                for(int j = 0; j < cardText.length; j++) {
                    if(cardText[j].toLowerCase().indexOf(text) > 0) {
                        v.add(arr[i]);
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
    
    public static void main(String[] argv) {
       // new CardUtil(true);
    }

    static class CombSearch implements CardSearch {
    
        private final CardSearch[] searches;
        
        CombSearch(CardSearch[] searches) {
            this.searches = searches;
        }

        public String getId(String nm) {
            for(int i = 0; i < searches.length; i++) {
                if(searches[i] == null) continue;
                String id = searches[i].getId(nm);
                if(id.equals("not found")) continue;
                return id;
            }
            return "not found";
        }

        public Set<String> getNames() {
            Set<String> ret = new HashSet<String>();
            for(int i = 0; i < searches.length; i++) {
                if(searches[i] == null) continue;
                ret.addAll(searches[i].getNames());
            }
            return ret;
        }

        public CardSet getAllCards() {
            Collection<CardEntry> c = new Vector<CardEntry>();
            for(int i = 0; i < searches.length; i++) {
                if(searches[i] == null) continue;
                Collections.addAll(c,searches[i].getAllCards().getCardArray());
            }
            return new SetImpl((CardEntry[]) c.toArray(new CardEntry[0]));
        }

        public CardEntry getCardById(String id) {
            for(int i = 0; i < searches.length; i++) {
                if(searches[i] == null) continue;
                CardEntry e = searches[i].getCardById(id);
                if(e != null) return e;
            }
            return null;
        }
        
        public CardSet searchByName(CardSet set, String name) {
            return searchByField(set,"Name:", name);
        }
        
        public CardSet searchByClan(CardSet set, String clan) {
            return searchByField(set, "Clan:", clan);
        }
        
        public CardSet searchByDiscipline(CardSet set, String disc) {
            return searchByField(set, "Discipline:", disc);
        }
        
        public CardSet searchByType(CardSet set, String type) {
            return searchByField(set, "Cardtype:", type);
        }
        
        public CardSet searchByText(CardSet set, String text) {
            CardEntry[] arr = set.getCardArray();
            text = text.toLowerCase();
            Vector<CardEntry> v = new Vector<CardEntry>();
            for(int i = 0; i < arr.length; i++) {
                String[] cardText = arr[i].getFullText();
                for(int j = 0; j < cardText.length; j++) {
                    if(cardText[j].toLowerCase().indexOf(text) > 0) {
                        v.add(arr[i]);
                        break;
                    }
                }
            }
            arr = new CardEntry[v.size()];
            v.toArray(arr);
            return new SetImpl(arr);
        }
    }
}
