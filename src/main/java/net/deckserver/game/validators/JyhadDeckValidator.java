package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;

import java.util.List;
import java.util.Set;

public class JyhadDeckValidator extends StandardDeckValidator {

    @Override
    public ValidationResult validate(Deck deck) {
        ValidationResult result = super.validate(deck);
        Set<String> cardsInInvalidSets = checkAgainstWhitelist(deck, List.of("Jyhad"));
        cardsInInvalidSets.stream()
                .map(this::getCardName)
                .forEach(invalidCard -> {
                    result.addError(String.format("%s is not allowed in this format.", invalidCard));
                });
        return result;
    }

    @Override
    public GameFormat supports() {
        return GameFormat.JYHAD;
    }
}
