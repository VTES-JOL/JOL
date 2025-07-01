package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;

import java.util.HashSet;
import java.util.Set;

public class ValidatorFactory {

    public static DeckValidator getDeckValidator(GameFormat format) {
        return switch (format) {
            case GameFormat.DUEL -> new DuelDeckValidator();
            case GameFormat.V5 -> new V5DeckValidator();
            case GameFormat.V5_STRICT -> new V5StrictDeckValidator();
            default -> new StandardDeckValidator();
        };
    }

    public static Set<String> getTags(Deck deck) {
        Set<String> tags = new HashSet<>();
        for (GameFormat gameFormat : GameFormat.values()) {
            DeckValidator validator = ValidatorFactory.getDeckValidator(gameFormat);
            ValidationResult validationResult = validator.validate(deck);
            if (validationResult.isValid()) {
                tags.add(gameFormat.toString());
            }
        }
        return tags;
    }
}
