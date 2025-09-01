package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class V5DeckValidator extends StandardDeckValidator {

    private static final List<String> sets;
    private static final List<String> cards;

    static {
        sets = IOUtils.readLines(Objects.requireNonNull(DuelDeckValidator.class.getResourceAsStream("valid-v5-sets.txt")), StandardCharsets.UTF_8);
        cards = IOUtils.readLines(Objects.requireNonNull(DuelDeckValidator.class.getResourceAsStream("valid-v5-cards.txt")), StandardCharsets.UTF_8);
    }

    @Override
    public ValidationResult validate(Deck deck) {
        ValidationResult result = super.validate(deck);
        Set<String> cardsInInvalidSets = checkAgainstWhitelist(deck, sets);
        Set<String> invalidCards = checkAgainstWhitelist(cardsInInvalidSets, cards);
        invalidCards.stream()
                .map(this::getCardName)
                .forEach(invalidCard -> {
                    result.addError(String.format("%s is not allowed in this format.", invalidCard));
                });
        return result;
    }

    @Override
    public GameFormat supports() {
        return GameFormat.V5;
    }

}
