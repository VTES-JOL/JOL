package net.deckserver.game.storage.cards;

import lombok.Getter;

@Getter
public enum Sect {
    CAMARILLA("Camarilla", "C"),
    SABBAT("Sabbat", "S"),
    INDEPENDENT("Independent", "I"),
    LAIBON("Laibon", "L"),
    ANARCH("Anarch", "A"),;

    private final String description;
    private final String code;

    Sect(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public static Sect of(String description) {
        for (Sect sect : values()) {
            if (sect.description.equalsIgnoreCase(description))
                return sect;
        }
        return null;
    }

    public static Sect startsWith(String description) {
        for (Sect sect : Sect.values()) {
            if (sect.description.toLowerCase().startsWith(description.toLowerCase())) {
                return sect;
            }
        }
        return null;
    }
}
