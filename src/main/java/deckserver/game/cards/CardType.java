package deckserver.game.cards;

import java.util.EnumSet;

/**
 * Created by shannon on 26/07/2016.
 */
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

    private String label;

    CardType(String label) {
        this.label = label;
    }

    public static CardType of(String label) {
        for (CardType type : EnumSet.allOf(CardType.class)) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        return COMBO;
    }

    public static EnumSet<CardType> cryptTypes() {
        return EnumSet.of(VAMPIRE, IMBUED);
    }

    public String getLabel() {
        return label;
    }
}
