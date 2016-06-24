/*
 * TranslateCardList.java
 *
 * Created on September 24, 2003, 12:40 PM
 */

package deckserver.cards;

import deckserver.interfaces.CardEntry;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Vector;

/**
 * @author administrator
 */
class TranslateCard implements CardEntry {

    static int cardCount = 0;
    private String id, name = "", descrip = "", type = "no type";
    private String[] text;
    private boolean advanced = false;
    private String group;

    TranslateCard(CardMap ids, String[] lines, int numLines) {
        text = new String[numLines];
        for (int i = 0; i < numLines; i++) {
            text[i] = lines[i];
            if (lines[i].startsWith("Advanced")) advanced = true;
            if (lines[i].startsWith("Group:")) group = lines[i].substring(7).trim();
            if (lines[i].startsWith("Cardtype:")) type = lines[i].substring(10).trim();
            if (lines[i].startsWith("Name:")) name = lines[i].substring(6).trim();
            if (lines[i].startsWith("Clan:")) descrip = lines[i].substring(6).trim();
            if (lines[i].startsWith("Level:")) advanced = true;
            if (lines[i].startsWith("Discipline:")) descrip = descrip + " " + lines[i].substring(12).trim();
        }
        if (!"Vampire".equals(type)) descrip = "";
        if (ids != null) id = ids.getId(name + (advanced ? " (advanced)" : ""));
    }

    static CardEntry[] readCards(CardMap map, LineNumberReader reader) {
        Vector<TranslateCard> v = new Vector<TranslateCard>();
        try {
            String[] lines = new String[25];
            int lineCount = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.length() > 0) {
                    lines[lineCount++] = line;
                } else {
                    v.add(new TranslateCard(map, lines, lineCount));
                    lines = new String[25];
                    lineCount = 0;
                }
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        CardEntry[] ret = new CardEntry[v.size()];
        v.toArray(ret);
        return ret;
    }

    public String getCardId() {
        return id;
    }

    public String[] getFullText() {
        return text;
    }

    public String getName() {
        return !advanced ? name : name + " (advanced)";
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public String getShortDescription() {
        return descrip;
    }

    public String getBaseName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public int compareTo(Object o) {
        CardEntry card = (CardEntry) o;
        return getName().compareTo(card.getName());
    }

    public String getType() {
        return type;
    }

    public boolean isCrypt() {
        return getType().equals(VAMPIRE) || getType().equals(IMBUED);
    }

    static class Counter {
        int i;

        Counter() {
            i = 1;
        }

        int incr() {
            return i++;
        }
    }

}
