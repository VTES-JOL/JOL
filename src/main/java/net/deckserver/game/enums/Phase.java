package net.deckserver.game.enums;

public enum Phase {
    UNLOCK("Unlock"),
    MASTER("Master"),
    MINION("Minion"),
    INFLUENCE("Influence"),
    DISCARD("Discard");

    private final String description;

    Phase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Phase of(String description) {
        for (Phase phase: Phase.values()) {
            if (phase.description.equals(description)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Not a valid phase");
    }
}
