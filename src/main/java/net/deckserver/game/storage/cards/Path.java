package net.deckserver.game.storage.cards;

import lombok.Getter;

@Getter
public enum Path {
    CAINE("Caine"),
    CATHARI("Cathari"),
    DEATH_AND_THE_SOUL("Death and the Soul"),
    POWER_AND_THE_INNER_VOICE("Power and the Inner Voice"),
    NONE("");

    private final String description;

    Path(String description) {
        this.description = description;
    }

    public static Path of(String description) {
        // exact match first
        for (Path path : Path.values()) {
            if (path.description.equalsIgnoreCase(description)) {
                return path;
            }
        }
        return NONE;
    }

    public static Path startsWith(String description) {
        for (Path path : Path.values()) {
            if (path.description.toLowerCase().startsWith(description.toLowerCase())) {
                return path;
            }
        }
        return NONE;
    }

}
