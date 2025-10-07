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
}
