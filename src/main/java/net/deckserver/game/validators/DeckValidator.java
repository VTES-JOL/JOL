package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;

public interface DeckValidator {

    ValidationResult validate(Deck deck);

    GameFormat supports();

}
