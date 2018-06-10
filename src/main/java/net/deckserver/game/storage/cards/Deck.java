/*
 * Deck.java
 *
 * Created on September 25, 2003, 8:45 PM
 */

package net.deckserver.game.storage.cards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class Deck {

    private static final Logger logger = LoggerFactory.getLogger(Deck.class);

    private final CardSearch search;
    private StringBuffer orig = new StringBuffer();
    private Collection<String> errors = new Vector<>();
    private int cryptSum = 0;
    private int deckSum = 0;
    private boolean parseCards = true;
    private Map<CardEntry, Integer> cards = new HashMap<>();
    private Set<String> groups = new TreeSet<>();
    private boolean valid;

    public Deck(CardSearch search, String deck) {
        this(search, deck, false, false);
    }

    public Deck(CardSearch search, String deck, boolean sizeOnly) {
        this(search, deck, sizeOnly, false);
    }

    public Deck(CardSearch search, String deck, boolean sizeOnly, boolean doConstruction) {
        this.search = search;
        if (deck != null) {
            if (sizeOnly && deck.startsWith("ZZZ@@@")) {
                //time();
                int idx1 = deck.indexOf("L");
                String sz = deck.substring(7, idx1);
                cryptSum = Integer.parseInt(sz);
                int idx2 = deck.indexOf("G");
                sz = deck.substring(idx1 + 1, idx2);
                deckSum = Integer.parseInt(sz);
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
                try {
                    processLine(line, ignoreTranslation);
                } catch (Exception e) {
                    logger.error("Error parsing deck line: " + line);
                }
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
        text = text.replaceAll("\\(advanced\\)", "(Adv)");
        CardEntry card = search.findCard(text);
        if (card == null) return null;
        return card;
    }

    public String[] getErrorLines() {
        return errors.toArray(new String[0]);
    }

    private void addCard(CardEntry card, int quantity) {
        if (card.isCrypt()) {
            cryptSum += quantity;
            if (!card.getGroup().toUpperCase().equals("ANY")) {
                groups.add(card.getGroup());
            }
        } else
            deckSum += quantity;
        if (cards.containsKey(card))
            quantity += getQuantity(card);
        cards.put(card, quantity);
    }

    public String getGroups() {
        if (groups.size() == 0) {
            return "none";
        } else {
            return groups.stream()
                    .collect(Collectors.joining("/"));
        }
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
        return cryptSum;
    }

    public int getLibSize() {
        return deckSum;
    }

    public boolean isValid() {
        return cryptSum >= 12 && deckSum >= 60 && deckSum <= 90 && validGroups();
    }

    public boolean validGroups() {
        if (this.groups.size() <= 1) {
            return true;
        } else if (this.groups.size() > 2) {
            return false;
        }
        String[] groupsArray = this.groups.toArray(new String[0]);
        // Get first group
        Integer first = Integer.valueOf(groupsArray[0]);
        // Is it within 1 group of second group
        Integer second = Integer.valueOf(groupsArray[1]);
        return (Math.abs(first - second) <= 1);
    }
}
