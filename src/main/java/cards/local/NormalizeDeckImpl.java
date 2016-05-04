/*
 * NormalizeCard.java
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
public class NormalizeDeckImpl implements NormalizeDeck {

    final CardSearch search;
    StringBuffer orig = new StringBuffer();
    StringBuffer translated = new StringBuffer();
    Collection<String> errors = new Vector<String>();
    boolean didTranslation = false;
    int cryptsum = 0;
    int decksum = 0;
    boolean parseCards = true;
    Map<CardEntry, Integer> cards = new HashMap<CardEntry, Integer>();
    private Collection<String> groups = new TreeSet<String>();

    public NormalizeDeckImpl(CardSearch search, String deck) {
        this(search, deck, false, false);
    }

    public NormalizeDeckImpl(CardSearch search, String deck, boolean sizeOnly) {
        this(search, deck, sizeOnly, false);
    }

    public NormalizeDeckImpl(CardSearch search, String deck, boolean sizeOnly, boolean doConstruction) {
        this.search = search;
        if (deck != null) {
            if (sizeOnly && deck.startsWith("ZZZ@@@")) {
                //time();
                int idx1 = deck.indexOf("L");
                String sz = deck.substring(7, idx1);
                cryptsum = Integer.parseInt(sz);
                int idx2 = deck.indexOf("G");
                sz = deck.substring(idx1 + 1, idx2);
                decksum = Integer.parseInt(sz);
                //time();
                int idx3 = deck.indexOf("@@@ZZZ");
                for (int i = idx2 + 1; i < idx3; i++) {
                    groups.add(deck.substring(i, i + 1));
                }
            } else {
                init(deck, doConstruction);
            }
        }
    }

    private void init(String deckin, boolean ignoreTranslation) {
        Reader r = new StringReader(deckin);
        LineNumberReader reader = new LineNumberReader(r);
        String line = null;
        try {
            while (reader.ready() && ((line = reader.readLine()) != null)) {
                if (line.startsWith("ZZZ@@@") && !ignoreTranslation) {
                    parseCards = false;
                    continue;
                }
                String id = processLine(line, ignoreTranslation);
                if (id != null) {
                    didTranslation = true;
                    translated.append("Z@" + id + "@Z");
                }
                translated.append(line);
                translated.append('\n');
            }
        } catch (IOException ie) {
            ie.printStackTrace(System.err);
        }
    }

    /* (non-Javadoc)
     * @see cards.local.NormalizeDeck#getDeckString()
     */
    public String getDeckString() {
        return orig.toString();
    }

    /* (non-Javadoc)
     * @see cards.local.NormalizeDeck#getFilteredDeck()
	 */
    public String getFilteredDeck() {
        return "ZZZ@@@C" + cryptsum + "L" + decksum + "G" + getGroups() + "@@@ZZZ\n" + translated.toString();
    }

    private String processLine(String nextLine, boolean ignoreTranslation) {
        nextLine = nextLine.trim();
        if (nextLine.length() == 0) {
            orig.append("\n");
            return null;
        }
        if (nextLine.startsWith("ZZZ@@@")) {
            return null;
        }
        String id = null;
        if (nextLine.startsWith("Z@")) {
            int idx = nextLine.indexOf("@Z");
            if (!ignoreTranslation)
                id = nextLine.substring(2, idx);
            nextLine = nextLine.substring(idx + 2);
        }
        orig.append(nextLine + "\n");
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
        // now, find the card
        CardEntry card = null;
        if (id != null) {
            card = search.getCardById(id);
        } else {
            if (!parseCards) return null;
            card = findCard(nextLine);
        }
        if (card != null) {
            addCard(card, num);
            if (id == null) return card.getCardId();
        }
        return null;
    }

    private CardEntry findCard(String text) {
        // first find out if advanced
        boolean advanced = false;
        if (text.endsWith("(advanced)")) {
            text = text.substring(0, text.lastIndexOf("(advanced)")).trim();
            advanced = true;
        }
        CardSet set = findCardName(text);
        CardEntry card = selectCard(text, set);
        if (card == null) return null;
        if (!advanced) return card;
        set = search.searchByText(set, "Advanced");
        card = selectCard(text, set);
        return card;
    }

    private CardEntry selectCard(String text, CardSet set) {
        text = text.toLowerCase();
        CardEntry[] cards = set.getCardArray();
        if (cards.length == 0) return null;
        CardEntry bestfit = cards[0];
        for (int i = 0; i < cards.length; i++) {
            //	System.err.println("FOO" + text + "FOO");
            // 	System.err.println("Testing against F " + cards[i].getBaseName() + "F");
            if (cards[i].getBaseName().toLowerCase().equals(text)) return cards[i];
            // 	System.err.println("No match");
            if (cards[i].getBaseName().toLowerCase().startsWith(text)) bestfit = cards[i];
        }
        return bestfit;
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#findCardName(java.lang.String)
	 */
    public CardSet findCardName(String text) {
        return NormalizeDeckFactory.findCardName(search, text, errors);
    }
    
 /*   private static final void time() {
    	System.out.println(System.currentTimeMillis());
    }*/

//    public static void main(String[] args) throws Throwable {
//    	JolAdminFactory admin = JolAdminFactory.INSTANCE = new JolAdmin("/home/deckserv/interactive/data");
//    	CardSearch search = admin.getBaseCards();
//    	String[] players = admin.getPlayers();
//    	for(int i = 0; i < players.length; i++) {
//    		String[] names = admin.getDeckNames(players[i]);
//    		for(int j = 0; j < names.length; j++) {
//    			String deck = admin.getDeck(players[i], names[j]);
//    			NormalizeDeck nd = NormalizeDeckFactory.constructDeck(search, deck);
//    			System.out.println(players[i] + " " + names[j] + " " + nd.getGroups());
//    			System.out.println(nd.getCards().length);
//    			return;
//    		}
//    	}
//    }
    
   /* 
    public static void main(String[] args) throws Throwable {
    	JolAdminFactory admin = JolAdminFactory.INSTANCE = new JolAdmin("/home/deckserv/interactive/data");
    	CardSearch search = admin.getBaseCards();
    	String[] players = admin.getPlayers();
    	for(int i = 0; i < players.length; i++) {
    		String[] names = admin.getDeckNames(players[i]);
    		for(int j = 0; j < names.length; j++) {
    			String deck = admin.getDeck(players[i], names[j]);
    			NormalizeDeck nd = NormalizeDeckFactory.getNormalizer(search, deck);
    			admin.createDeck(players[i], names[j], nd.getFilteredDeck());
    			System.out.println(players[i] + " " + names[j]);
    		}
    	}
    }*/
    
 /*   public static void main(String[] args) throws Throwable {
    	JolAdminFactory admin = JolAdminFactory.INSTANCE = new JolAdmin("/home/deckserv/interactive/data");
    	CardSearch search = admin.getBaseCards();
    	String deck = StreamReader.read(new FileInputStream("/gfink/priv/ds/cards/src/data/card.test"));
    	NormalizeDeck nd = NormalizeDeckFactory.getNormalizer(search, deck);
    	String[] err = nd.getErrorLines();
    	for(int i = 0; i < err.length; i++)
    		System.out.println(err[i]);
    }*/

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#getErrorLines()
	 */
    public String[] getErrorLines() {
        return (String[]) errors.toArray(new String[0]);
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#addCard(cards.model.CardEntry)
	 */
    public void addCard(CardEntry card) {
        addCard(card, 1);
        String newtxt = "1x " + card.getName();
        orig.append("\n" + newtxt);
        translated.append("\n" + "Z@" + card.getCardId() + "@Z");
        translated.append(newtxt);
    }

    private void addCard(CardEntry card, int quantity) {
//    	System.err.println("adding " + card.getName() + " " + card.isCrypt());
        if (card.isCrypt()) {
            cryptsum += quantity;
            groups.add(card.getGroup());
        } else
            decksum += quantity;
        if (cards.containsKey(card))
            quantity += getQuantity(card);
        cards.put(card, new Integer(quantity));
    }

    public String getGroups() {
        StringBuffer gps = new StringBuffer();
        for (Iterator<String> i = groups.iterator(); i.hasNext(); ) {
            gps.append(i.next());
        }
        return gps.toString();
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#getCards()
	 */
    public CardEntry[] getCards() {
        CardEntry[] ret = new CardEntry[cards.size()];
        cards.keySet().toArray(ret);
        return ret;
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#getQuantity(cards.model.CardEntry)
	 */
    public int getQuantity(CardEntry card) {
        Integer i = cards.get(card);
        return i.intValue();
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#removeAllCards()
	 */
    public void removeAllCards() {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#removeCard(cards.model.CardEntry)
	 */
    public void removeCard(CardEntry card) {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
    }

    /* (non-Javadoc)
	 * @see cards.local.NormalizeDeck#setQuantity(cards.model.CardEntry, int)
	 */
    public void setQuantity(CardEntry card, int quantity) {
        throw new UnsupportedOperationException("Can't remove from this deck impl");
        //cards.put(card, new Integer(quantity));
    }

    public int getCryptSize() {
        return cryptsum;
    }

    public int getLibSize() {
        return decksum;
    }

}
