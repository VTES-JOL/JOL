package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;

public class PlayTestValidator extends StandardDeckValidator {

    @Override
    public ValidationResult validate(Deck deck) {
        return super.validate(deck, true);
    }

    @Override
    public GameFormat supports() {
        return GameFormat.PLAYTEST;
    }
}
