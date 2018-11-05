/*
 * CardEntry.java
 *
 * Created on September 23, 2003, 9:02 PM
 */

package net.deckserver.game.storage.cards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author administrator
 */
public class CardEntry {

    private String id;
    private String name = "";
    private String type = "no type";
    private String[] text;
    private String group;

    CardEntry(Map<String, String> ids, List<String> lines) {
        text = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            text[i] = currentLine;
            if (currentLine.startsWith("Group:")) group = currentLine.replaceAll("Group:", "").trim();
            if (currentLine.startsWith("Cardtype:")) type = currentLine.replaceAll("Cardtype:", "").trim();
            if (currentLine.startsWith("Name:")) name = currentLine.replaceAll("Name:", "").trim();
        }
        if (ids != null) id = ids.get(name.toLowerCase());
    }

    public String getCardId() {
        return id;
    }

    public String[] getFullText() {
        return text;
    }

    public String getText() {
        return Arrays.toString(text);
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public CardType getCardType() {
        return CardType.of(type);
    }

    public String getTypeClass() {
        return Arrays.stream(type.toLowerCase()
                .trim().split("/"))
                .map(s -> s.replaceAll(" ", "_"))
                .sorted()
                .collect(Collectors.joining(" "));
    }

    public boolean isCrypt() {
        return CardType.cryptTypes().contains(getCardType());
    }

    public boolean isLibrary() {
        return CardType.libraryTypes().contains(getCardType());
    }

    public boolean hasLife() {
        return CardType.lifeTypes().contains(getCardType());
    }

    public boolean hasBlood() {
        return CardType.VAMPIRE.equals(getCardType());
    }

    @Override
    public String toString() {
        return "CardEntry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
