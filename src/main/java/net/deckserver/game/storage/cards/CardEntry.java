/*
 * CardEntry.java
 *
 * Created on September 23, 2003, 9:02 PM
 */

package net.deckserver.game.storage.cards;

import net.deckserver.game.json.deck.CardSummary;

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
    private boolean crypt;

    CardEntry(CardSummary cardSummary) {
        this.id = cardSummary.getKey();
        this.name = cardSummary.getDisplayName();
        this.type = cardSummary.getType();
        this.text = cardSummary.getText().split("\n");
        this.group = cardSummary.getGroup();
        this.crypt = cardSummary.isCrypt();
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
        return crypt;
    }

    public boolean isLibrary() {
        return !crypt;
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
