package net.deckserver.game.validators;

import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.system.GameFormat;

import java.util.Set;

public class StandardDeckValidator extends AbstractDeckValidator {

    private final static int MIN_CRYPT = 12;
    private final static int MIN_LIBRARY = 60;
    private final static int MAX_LIBRARY = 90;

    protected static boolean validGroups(Set<String> groups) {
        if (groups.size() <= 1) {
            return true;
        } else if (groups.size() > 2) {
            return false;
        }
        String[] groupsArray = groups.toArray(new String[0]);
        // Get the first group
        Integer first = Integer.valueOf(groupsArray[0]);
        // Is it within 1 group of the second group?
        Integer second = Integer.valueOf(groupsArray[1]);
        return (Math.abs(first - second) <= 1);
    }

    public ValidationResult validate(Deck deck) {
        return validate(deck, false);
    }

    public ValidationResult validate(Deck deck, boolean includePlaytestCards) {
        ValidationResult result = new ValidationResult();
        Set<String> groups = getGroups(deck);
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
        if (!validGroups(groups)) {
            result.getErrors().add(String.format("Invalid groups: %s", String.join(", ", groups)));
        }
        findBannedCards(deck).forEach(bannedCard -> {
            result.addError(String.format("%s is banned", bannedCard));
        });
        if (!includePlaytestCards) {
            findPlaytestCards(deck).forEach(playtestCard -> {
                result.addError(String.format("%s is not legal yet.", playtestCard));
            });
        }
        return result;

    }

    @Override
    public GameFormat supports() {
        return GameFormat.STANDARD;
    }

}
