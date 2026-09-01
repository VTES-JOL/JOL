package net.deckserver.game.validators;

import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.game.cards.CryptCard;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.LibraryCard;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractDeckValidator implements DeckValidator {

    private Stream<CardCount> buildStream(Deck deck) {
        Stream<CardCount> cryptStream = deck.getCrypt().getCards().stream();
        Stream<CardCount> libraryStream = deck.getLibrary().getCards().stream().map(LibraryCard::getCards).flatMap(Collection::stream);
        return Stream.concat(cryptStream, libraryStream);
    }

    protected Stream<Card> cardStream(Deck deck) {
        return buildStream(deck)
                .map(CardCount::getId)
                .map(String::valueOf)
                .distinct()
                .map(CardRegistry::findById)
                .filter(Objects::nonNull);
    }

    protected String getCardName(String id) {
        Card card = CardRegistry.findById(id);
        return card != null ? card.displayName() : id;
    }

    protected Set<String> getGroups(Deck deck) {
        Set<String> groups = new HashSet<>();
        if (deck.getCrypt() == null) {
            return Collections.emptySet();
        }
        for (CardCount cardCount : deck.getCrypt().getCards()) {
            Card card = CardRegistry.findById(String.valueOf(cardCount.getId()));
            if (card instanceof CryptCard crypt && !crypt.group().equalsIgnoreCase("ANY")) {
                groups.add(crypt.group());
            }
        }
        return groups;
    }

    protected Set<String> findBannedCards(Deck deck) {
        return cardStream(deck)
                .filter(Card::banned)
                .map(Card::displayName)
                .collect(Collectors.toSet());
    }

    protected Set<String> findPlaytestCards(Deck deck) {
        return cardStream(deck)
                .filter(Card::playtest)
                .map(Card::displayName)
                .collect(Collectors.toSet());
    }

    protected Set<String> checkAgainstWhitelist(Deck deck, List<String> validSets) {
        return cardStream(deck).filter(card -> {
                    Set<String> cardSets = new HashSet<>(card.sets());
                    cardSets.retainAll(validSets);
                    return cardSets.isEmpty();
                }).map(Card::id)
                .collect(Collectors.toSet());
    }

    protected Set<String> checkAgainstWhitelist(Set<String> ids, List<String> whitelist) {
        Set<String> outsideWhitelist = new HashSet<>(ids);
        whitelist.forEach(outsideWhitelist::remove);
        return outsideWhitelist;
    }
}
