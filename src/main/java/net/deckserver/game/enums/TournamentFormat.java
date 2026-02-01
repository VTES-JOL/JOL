package net.deckserver.game.enums;

import lombok.Data;

public enum TournamentFormat {
    SINGLE_DECK("Single Deck"),
    MULTI_DECK("Multi-Deck");

    private final String description;

    TournamentFormat(String description) {
        this.description = description;
    }
}
