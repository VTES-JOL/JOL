/*
 * TranslateCardList.java
 *
 * Created on September 24, 2003, 12:40 PM
 */

package deckserver.game.cards;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author administrator
 */
class CardEntryDetail implements CardEntry {

    private String id;
    private String name = "";
    private String type = "no type";
    private String[] text;
    private boolean advanced = false;
    private String group;

    private CardEntryDetail(CardMap ids, String[] lines, int numLines) {
        text = new String[numLines];
        for (int i = 0; i < numLines; i++) {
            text[i] = lines[i];
            if (lines[i].startsWith("Advanced")) advanced = true;
            if (lines[i].startsWith("Group:")) group = lines[i].substring(7).trim();
            if (lines[i].startsWith("Cardtype:")) type = lines[i].substring(10).trim();
            if (lines[i].startsWith("Name:")) name = lines[i].substring(6).trim();
            if (lines[i].startsWith("Level:")) advanced = true;
        }
        if (ids != null) id = ids.getId(name + (advanced ? " (ADV)" : ""));
    }

    static List<CardEntry> readCards(CardMap map, LineNumberReader reader) {
        List<CardEntry> entries = new ArrayList<>();
        try {
            String[] lines = new String[25];
            int lineCount = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.length() > 0) {
                    lines[lineCount++] = line;
                } else {
                    entries.add(new CardEntryDetail(map, lines, lineCount));
                    lines = new String[25];
                    lineCount = 0;
                }
            }
        } catch (IOException ie) {
            ie.printStackTrace();
            throw new RuntimeException("Unable to read cards from card file");
        }
        return entries;
    }

    @Override
    public String getCardId() {
        return id;
    }

    @Override
    public String[] getFullText() {
        return text;
    }

    @Override
    public String getText() {
        return Arrays.toString(text);
    }

    @Override
    public String getName() {
        return !advanced ? name : name + " (ADV)";
    }

    @Override
    public String getBaseName() {
        return name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isCrypt() {
        return CardType.cryptTypes().contains(CardType.of(type));
    }

}
