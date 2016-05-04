/*
 * ND.java
 *
 * Created on December 30, 2004, 8:46 AM
 */

package cards.local;

import cards.model.CardEntry;
import cards.model.CardSearch;
import cards.model.CardSet;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * Normalizes deck inputlines to 1x <cardname> form
 *
 * @author gfinklan
 */
public class ND implements Iterator, NormalizeDeck {

    final CardSearch search;
    String orig;
    LineNumberReader reader;
    String nextLine = null;
    CardEntry card = null;
    int quantity = -1;
    Collection<String> errors = new Vector<String>();
    Map<CardEntry, Integer> cards = new HashMap<CardEntry, Integer>();

    public ND(CardSearch search, String deck) {
        this(search, deck, true);
    }

    public ND(CardSearch search, String deck, boolean iterate) {
        this.search = search;
        if (deck != null) {
            orig = deck;
            Reader r = new StringReader(deck);
            reader = new LineNumberReader(r);
            do {
                calculateNext();
            } while (iterate && nextLine != null);
        }
    }

    public static CardSet findCardName(CardSearch search, String text, Collection<String> errors) {
        //        System.out.println("Finding card " + text);
        // check for exact matches first
        //  CardEntry card = search.getCardByName(text);
        //    if(card != null) {
        //       System.out.println("Searching prefix");
        CardSet cards = search.getAllCards();
        // check for prefixes
        CardSet set = search.searchByName(cards, text);
        //    System.out.println("Initial size " + set.getCardArray().length);
        if (set.getCardArray().length > 0) return set;
        // check for abbreviations
        text = text.toLowerCase();
        //         System.out.println("Searching abbreviations");
        String id = search.getId(text);
        //    System.out.println("id is " + id);
        if (id == null || id.equals("not found"))
            // check for abbreviation prefixes
            for (Iterator<?> i = search.getNames().iterator(); i.hasNext(); ) {
                String abbrev = (String) i.next();
                //     System.out.println("Checking " + abbrev);
                if (abbrev.startsWith(text)) {
                    //         System.out.println("Found abbrev " + abbrev);
                    id = search.getId(abbrev);
                    break;
                }
            }
        if (id.equals("not found")) id = null;
        if (id != null) {
            // now need to convert this to a set to handle advanced vamps
            CardEntry card = search.getCardById(id);
            set = search.searchByName(cards, card.getBaseName());
        }
        if (id == null || set.getCardArray().length == 0) {
            //   }
            // couldn't find any match
            //          System.out.println("No match");
            errors.add(text);
        }
        //  System.out.println("Returning " + set.getCardArray().length);
        return set;
    }

    public String parseLine(String line) {
        nextLine = line;
        processLine();
        return nextLine;
    }

    public String getDeckString() {
        return orig;
    }

    public CardEntry getCard() {
        return card;
    }

    public int getQuantity() {
        return quantity;
    }

    private void calculateNext() {
        try {
            if (!reader.ready()) nextLine = null;
            nextLine = reader.readLine();
            if (nextLine != null) {
                nextLine = nextLine.trim();
                if (nextLine.equals(""))
                    nextLine = null;
                else
                    processLine();
                if (nextLine == null) calculateNext();
            }
        } catch (IOException ie) {
            nextLine = null;
        }
//        if(nextLine == null) System.out.println("Found no card at all");
//        else {
        //           System.out.println("Found card " + nextLine);
        //      }
    }

    private void processLine() {
        // first, find the number
        int num = 1;
        if (nextLine.matches("\\d+\\s*x.*")) {
            int x = nextLine.indexOf("x");
            String numStr = nextLine.substring(0, x).trim();
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException nfe) {
                // should not happen
                num = -1;
            }
            nextLine = nextLine.substring(x + 1).trim();
        }
        quantity = num;
        // now, find the card
        nextLine = findCard(nextLine);
        if (nextLine != null) {
            nextLine = num + "x " + nextLine;
            addCard(card, quantity);
        }
    }

    private String findCard(String text) {
        //       System.out.println("Text in " + text);
        // first find out if advanced
        boolean advanced = false;
        if (text.endsWith("(advanced)")) {
            text = text.substring(0, text.lastIndexOf("(advanced)")).trim();
            advanced = true;
        }
        CardSet set = findCardName(text);
        card = selectCard(text, set);
        if (card == null) return null;
        //      System.out.println("Text out " + card.getName());
        if (!advanced) return card.getName();
        set = search.searchByText(set, "Advanced");
        card = selectCard(text, set);
        if (card == null) return null;
        //       System.out.println("Text out " + card.getName());
        return card.getName();
    }

    private CardEntry selectCard(String text, CardSet set) {
        text = text.toLowerCase();
        CardEntry[] cards = set.getCardArray();
        if (cards.length == 0) return null;
        for (int i = 0; i < cards.length; i++) {
            //         System.out.println("Finding prefix of " + text + " with card " + cards[i].getName());
            if (cards[i].getBaseName().toLowerCase().startsWith(text)) return cards[i];
        }
        return cards[0];
    }

    public CardSet findCardName(String text) {
        return findCardName(search, text, errors);
    }

    public boolean hasNext() {
        return nextLine != null;
    }

    public Object next() {
        //     System.out.println("Returning " + nextLine);
        try {
            return nextLine;
        } finally {
            calculateNext();
        }
    }

    public void remove() {
        // not supported
    }
    
    /*
    public static void main(String[] args) throws Throwable {
        InputStream in = new FileInputStream(args[0]);
        String deck = StreamReader.read(in);
        ND nd = new NormalizeDeck(deck,true);
        PrintWriter w = new PrintWriter(new ByteArrayOutputStream());
        printDeckHtml(nd,w);
        String[] errors = nd.getErrorLines();
        for(int i = 0; i < errors.length; i++) {
            System.out.println("Error : " + errors[i]);
        }
    }*/
    
  /*  public static void printDeckHtml(final Deck deck, PrintWriter out) throws IOException {
        final Map categoryMap = new HashMap();
        categoryMap.put("Vampire",new Integer(1));
        categoryMap.put("Master",new Integer(2));
        categoryMap.put("Event",new Integer(3));
        CardEntry[] cards = deck.getCards();
        SortedSet set = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                CardEntry c1 = (CardEntry) o1;
                CardEntry c2 = (CardEntry) o2;
                String t1 = c1.getType();
                String t2 = c2.getType();
                if(t1.equals(t2)) {
                    int i1 = deck.getQuantity(c1);
                    int i2 = deck.getQuantity(c2);
                    if(i1 == i2) return c1.getName().compareTo(c2.getName());
                    return i2 - i1;
                }
                Integer cat1 = (Integer) categoryMap.get(t1);
                Integer cat2 = (Integer) categoryMap.get(t2);
                if(cat1 == null && cat2 == null)
                    return t1.compareTo(t2);
                if(cat1 == null) return 1;
                if(cat2 == null) return -1;
                return cat1.intValue() - cat2.intValue();
            }
        });
        set.addAll(Arrays.asList(cards));
        out.println("<br/>");
        String curType = null;
        StringBuffer currentContents = new StringBuffer();
        int counter = 0;
        int deckCounter = -1;
        int vampCounter = -1;
        for(Iterator i = set.iterator();i.hasNext();) {
            CardEntry cur = (CardEntry) i.next();
            if(curType != null & !cur.getType().equals(curType)) {
                out.println("<b>" + curType + "(" + counter + ")</b><br/>");
                out.println(currentContents.toString());
                currentContents = new StringBuffer();
                if(curType.equals("Vampire")) {
                    deckCounter = 0;
                    vampCounter = counter;
                }
                counter = 0;
                curType = cur.getType();
            }
            if(curType == null) {
                curType = cur.getType();
                if(!curType.equals("Vampire")) deckCounter = 0;
            }
            int quantity = deck.getQuantity(cur);
            counter += quantity;
            if(deckCounter >= 0) deckCounter += quantity;
            currentContents.append(quantity);
            currentContents.append(" ");
        //    currentContents.append(" <a href=" + params.getPrefix() + params.getCardUrl() + cur.getCardId() + ">" + cur.getName() + "</a><br/>");
            //   out.println(deck.getQuantity(cur));
            //       out.println(" <a href=" + params.getPrefix() + params.getCardUrl() + cur.getCardId() + ">" + cur.getName() + "</a><br/>");
        }
        out.println("<b>" + curType + "(" + counter + ")</b><br/>");
        out.println(currentContents.toString());
        out.println("<b>Total library: " + deckCounter + "</b>");
        System.out.println("V " + vampCounter + " L " + deckCounter);
    } */

    public String[] getErrorLines() {
        return (String[]) errors.toArray(new String[0]);
    }

    public void addCard(CardEntry card) {
        addCard(card, 1);
    }

    private void addCard(CardEntry card, int quantity) {
        if (cards.containsKey(card))
            quantity += getQuantity(card);
        cards.put(card, new Integer(quantity));
    }

    public CardEntry[] getCards() {
        CardEntry[] ret = new CardEntry[cards.size()];
        cards.keySet().toArray(ret);
        return ret;
    }

    public int getQuantity(CardEntry card) {
        Integer i = cards.get(card);
        return i.intValue();
    }

    public void removeAllCards() {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
    }

    public void removeCard(CardEntry card) {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
    }

    public void setQuantity(CardEntry card, int quantity) {
        cards.put(card, new Integer(quantity));
        //      System.err.println("Adding to deck " + card.getName());
    }

    public String getFilteredDeck() {
        return orig;
    }

    public int getCryptSize() {
        int sum = 0;
        for (Iterator<CardEntry> i = cards.keySet().iterator(); i.hasNext(); ) {
            CardEntry c = i.next();
            if (c.isCrypt())
                sum += getQuantity(c);
        }
        return sum;
    }

    public int getLibSize() {
        int sum = 0;
        for (Iterator<CardEntry> i = cards.keySet().iterator(); i.hasNext(); ) {
            CardEntry c = i.next();
            if (!c.isCrypt())
                sum += getQuantity(c);
        }
        return sum;
    }

    public String getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

}
