package net.deckserver.game.validators;

import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class DuelDeckValidator extends StandardDeckValidator {

    private static final List<String> validNames;
    private final static int MIN_CRYPT = 12;
    private final static int MIN_LIBRARY = 40;
    private final static int MAX_LIBRARY = 60;

    static {
        validNames = IOUtils.readLines(Objects.requireNonNull(DuelDeckValidator.class.getResourceAsStream("valid-duel-format-cards.txt")), StandardCharsets.UTF_8);
    }

    @Override
    public ValidationResult validate(Deck deck) {
        ValidationResult result = new ValidationResult();
        int cryptCount = deck.getCrypt().getCount();
        int libraryCount = deck.getLibrary().getCount();
        if (cryptCount < MIN_CRYPT) {
            result.getErrors().add(String.format("Invalid crypt count: %d is below the minimum of %d.", cryptCount, MIN_CRYPT));
        }
        if (libraryCount < MIN_LIBRARY) {
            result.getErrors().add(String.format("Invalid library count: %d is below the minimum of %d.", libraryCount, MIN_LIBRARY));
        }
        if (libraryCount > MAX_LIBRARY) {
            result.getErrors().add(String.format("Invalid library count: %d is above the maximum of %d.", libraryCount, MAX_LIBRARY));
        }
        validIds(deck, result);
        return result;
    }

    @Override
    public GameFormat supports() {
        return GameFormat.DUEL;
    }

    private void validIds(Deck deck, ValidationResult result) {
        cardSummaryStream(deck).map(CardSummary::getDisplayName)
                .filter(cardName -> !validNames.contains(cardName))
                .forEach(invalidCard -> {
                    result.addError(String.format("%s is not allowed in this format.", invalidCard));
                });
    }

}
