package net.deckserver.rest.bean;

import net.deckserver.storage.json.deck.ExtendedDeck;

/**
 * @param deckId the persisted deck's id when this edit corresponds to a saved
 *               deck (load/save), or null for a transient validate/empty edit.
 */
public record DeckEdit(ExtendedDeck deck, String contents, String deckId) {
    public static final DeckEdit EMPTY = new DeckEdit(null, null, null);
}
