package net.deckserver.game.storage.cards;

import lombok.Getter;

import java.util.EnumSet;

/**
 * Created by shannon on 26/07/2016.
 */
@Getter
public enum CardType {

    VAMPIRE("Vampire"),
    IMBUED("Imbued"),
    MASTER("Master"),
    ACTION("Action"),
    MODIFIER("Action Modifier"),
    REACTION("Reaction"),
    COMBAT("Combat"),
    ALLY("Ally"),
    RETAINER("Retainer"),
    POLITICAL("Political Action"),
    EQUIPMENT("Equipment"),
    EVENT("Event"),
    COMBO("Combo");

    private final String label;

    CardType(String label) {
        this.label = label;
    }

    public static CardType of(String label) {
        for (CardType type : EnumSet.allOf(CardType.class)) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        return COMBO;
    }

    public static EnumSet<CardType> lifeTypes() {
        return EnumSet.of(ALLY, RETAINER, IMBUED);
    }

}
