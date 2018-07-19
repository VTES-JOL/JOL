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
    private Collection<String> errors = new Vector<>();
    private int cryptSum = 0;
    private int deckSum = 0;
    private Map<CardEntry, Integer> cards = new HashMap<>();
    private Set<String> groups = new TreeSet<>();

    public Deck(CardSearch search, String deck) {
        this.search = search;
        if (deck != null) {
            Reader r = new StringReader(deck);
            LineNumberReader reader = new LineNumberReader(r);
            String line = null;
            try {
                while (reader.ready() && ((line = reader.readLine()) != null)) {
                    try {
                        processLine(line);
                    } catch (Exception e) {
                        errors.add(line);
                    }
                }
            } catch (IOException ie) {
                logger.error("Error reading deck: {}", ie);
            }
        }
    }

    private void processLine(String nextLine) {
        nextLine = nextLine.trim();
        if (nextLine.isEmpty()) {
            return;
        }
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
        CardEntry card;
        card = findCard(nextLine);
        if (card != null) {
            addCard(card, num);
        }
    }

    private CardEntry findCard(String text) {
        text = text.replaceAll("\\(advanced\\)", "(Adv)");
        return search.findCard(text);
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
