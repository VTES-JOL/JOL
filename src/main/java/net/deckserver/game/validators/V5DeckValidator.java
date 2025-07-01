package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class V5DeckValidator extends StandardDeckValidator {

    private static final List<String> sets;

    static {
        sets = IOUtils.readLines(Objects.requireNonNull(DuelDeckValidator.class.getResourceAsStream("valid-v5-sets.txt")), StandardCharsets.UTF_8);
    }

    @Override
    public ValidationResult validate(Deck deck) {
        ValidationResult result = super.validate(deck);
        inSets(deck, result, sets);
        return result;
    }

    @Override
    public GameFormat supports() {
        return GameFormat.V5;
    }

}
