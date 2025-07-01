package net.deckserver.game.validators;

import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.LibraryCard;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractDeckValidator implements DeckValidator {

    private final static CardSearch cardSearch = CardSearch.INSTANCE;

    private Stream<CardCount> buildStream(Deck deck) {
        Stream<CardCount> cryptStream = deck.getCrypt().getCards().stream();
        Stream<CardCount> libraryStream = deck.getLibrary().getCards().stream().map(LibraryCard::getCards).flatMap(Collection::stream);
        return Stream.concat(cryptStream, libraryStream);
    }

    protected Stream<CardSummary> cardSummaryStream(Deck deck) {
        return buildStream(deck)
                .map(CardCount::getId)
                .map(String::valueOf)
                .distinct()
                .map(cardSearch::get);
    }

    protected CardSummary getCardSummary(String cardId) {
        return cardSearch.get(cardId);
    }

    protected Set<String> getGroups(Deck deck) {
        Set<String> groups = new HashSet<>();
        if (deck.getCrypt() == null) {
            return Collections.emptySet();
        }
        for (CardCount cardCount : deck.getCrypt().getCards()) {
            String id = String.valueOf(cardCount.getId());
            CardSummary card = cardSearch.get(id);
            if (!card.getGroup().equalsIgnoreCase("ANY")) {
                groups.add(card.getGroup());
            }
        }
        return groups;
    }

    protected void checkForBannedCards(Deck deck, ValidationResult result) {
        cardSummaryStream(deck)
                .filter(CardSummary::isBanned)
                .forEach(bannedCard -> {
                    result.addError(String.format("%s is banned", bannedCard.getDisplayName()));
                });
    }

    protected void inSets(Deck deck, ValidationResult result, List<String> validSets) {
        cardSummaryStream(deck).filter(cardSummary -> {
            Set<String> cardSets = new HashSet<>(cardSummary.getSets());
            cardSets.retainAll(validSets);
            return cardSets.isEmpty();
        }).forEach(bannedCard -> {
            result.addError(String.format("%s is not allowed in this format.", bannedCard.getDisplayName()));
        });
    }
}
