package net.deckserver.game.validators;

import net.deckserver.game.enums.GameFormat;
import net.deckserver.storage.json.deck.Deck;

public interface DeckValidator {

    ValidationResult validate(Deck deck);

    GameFormat supports();

}
