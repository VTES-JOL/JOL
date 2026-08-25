package net.deckserver.dwr.model;

import net.deckserver.storage.json.deck.ExtendedDeck;

public record DeckEdit(ExtendedDeck deck, String contents) {
    public static final DeckEdit EMPTY = new DeckEdit(null, null);
}
