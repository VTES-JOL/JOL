/*
 * NormalizeCard.java
 *
 * Created on December 30, 2004, 8:46 AM
 */

package deckserver.game.cards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DeckImpl implements Deck {

    private static final Logger logger = LoggerFactory.getLogger(DeckImpl.class);

    final CardSearch search;
    StringBuffer orig = new StringBuffer();
    StringBuffer translated = new StringBuffer();
    Collection<String> errors = new Vector<>();
    boolean didTranslation = false;
    int cryptsum = 0;
    int decksum = 0;
    boolean parseCards = true;
    Map<CardEntry, Integer> cards = new HashMap<>();
    private Collection<String> groups = new TreeSet<>();

    public DeckImpl(CardSearch search, String deck) {
        this(search, deck, false, false);
    }

    public DeckImpl(CardSearch search, String deck, boolean sizeOnly) {
        this(search, deck, sizeOnly, false);
    }

    public DeckImpl(CardSearch search, String deck, boolean sizeOnly, boolean doConstruction) {
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
                    translated.append("Z@").append(id).append("@Z");
                }
                translated.append(line);
                translated.append('\n');
            }
        } catch (IOException ie) {
            logger.error("Error reading deck: {}", ie);
        }
    }

    public String getDeckString() {
        return orig.toString();
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
        orig.append(nextLine).append("\n");
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
        for (CardEntry card : cards) {
            if (card.getBaseName().toLowerCase().equals(text)) return card;
            if (card.getBaseName().toLowerCase().startsWith(text)) bestfit = card;
        }
        return bestfit;
    }

    public CardSet findCardName(String text) {
        return DeckFactory.findCardName(search, text, errors);
    }

    public String[] getErrorLines() {
        return errors.toArray(new String[0]);
    }

    private void addCard(CardEntry card, int quantity) {
        if (card.isCrypt()) {
            cryptsum += quantity;
            groups.add(card.getGroup());
        } else
            decksum += quantity;
        if (cards.containsKey(card))
            quantity += getQuantity(card);
        cards.put(card, quantity);
    }

    public String getGroups() {
        StringBuilder gps = new StringBuilder();
        groups.forEach(gps::append);
        return gps.toString();
    }

    public CardEntry[] getCards() {
        CardEntry[] ret = new CardEntry[cards.size()];
        cards.keySet().toArray(ret);
        return ret;
    }

    public int getQuantity(CardEntry card) {
        return cards.get(card);
    }

    public int getCryptSize() {
        return cryptsum;
    }

    public int getLibSize() {
        return decksum;
    }

}
