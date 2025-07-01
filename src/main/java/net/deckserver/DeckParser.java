package net.deckserver;

import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.deck.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class DeckParser {

    private final static Logger logger = LoggerFactory.getLogger(DeckParser.class);

    private final static Pattern HEADER_PATTERN = Pattern.compile("^(crypt|library|master|action|combat|reaction|all(y|ies)|combo|equipment|event|political).*");
    private final static Pattern LEGACY_JOL = Pattern.compile("^z@.*@z|^zzz.*");
    private final static Pattern COUNT_PATTERN = Pattern.compile("^(\\d{1,2}(?!\\d|st|nd|rd|th))\\s*x?\\s*(.*)$");
    private final static Pattern ADVANCED_PATTERN = Pattern.compile("advanced");
    private final static Pattern COMMENT_PATTERN = Pattern.compile("^#.*");
    private final static Pattern WINDOWS_QUOTE_PATTERN = Pattern.compile("`");
    private final static Pattern EXTRA_SPACE_PATTERN = Pattern.compile("\\s{2,}");
    private final static Pattern DISCIPLINE_PATTERN = Pattern.compile("\\s(-none-|none|abo|ani|aus|cel|chi|dai|def|dem|dom|for|inn|jud|mar|mel|myt|nec|obe|obf|obt|pot|pre|pro|qui|red|san|ser|spi|tem|tha|thn|val|ven|vic|vin|vis|viz)");
    private final static CardSearch cardSearch = CardSearch.INSTANCE;
    private static final Predicate<CardCount> IS_CRYPT = (item) -> cardSearch.get(String.valueOf(item.getId())).isCrypt();
    private static final Predicate<CardCount> FOUND_CARD = (item) -> item.getId() != null;
    private static final Function<CardCount, String> TYPE_MAPPER = (item) -> cardSearch.get(String.valueOf(item.getId())).getType();
    public static final BinaryOperator<CardCount> CARD_MERGE = (a, b) -> new CardCount(b.getId(), b.getName(), a.getCount() + b.getCount(), b.getComments());
    public static final Function<Map<Integer, Optional<CardCount>>, List<CardCount>> CARD_MAPPER = map -> map.values().stream().flatMap(Optional::stream).collect(toList());

    public static ExtendedDeck parseDeck(String contents) {
        Deck deck = new Deck();

        List<CardCount> cardCounts = contents.lines()
                .map(DeckParser::parseLine)
                .flatMap(Optional::stream)
                .toList();
        List<CardCount> foundCards = cardCounts.stream().filter(FOUND_CARD).toList();
        List<CardCount> cryptCards = foundCards.stream().filter(IS_CRYPT)
                .collect(collectingAndThen(groupingBy(CardCount::getId, reducing(CARD_MERGE)), CARD_MAPPER));
        int cryptCount = cryptCards.stream().map(CardCount::getCount).reduce(0, Integer::sum);
        Crypt crypt = new Crypt();
        crypt.setCards(cryptCards);
        crypt.setCount(cryptCount);
        deck.setCrypt(crypt);

        Map<String, List<CardCount>> libraryCards = foundCards.stream().filter(IS_CRYPT.negate()).collect(groupingBy(TYPE_MAPPER));
        Library library = new Library();
        List<LibraryCard> libraryCardList = new ArrayList<>();
        libraryCards.forEach((type, list) -> {
            List<CardCount> collapsedList = list.stream().collect(collectingAndThen(groupingBy(CardCount::getId, reducing(CARD_MERGE)), CARD_MAPPER));
            int typeCount = collapsedList.stream().map(CardCount::getCount).reduce(0, Integer::sum);
            LibraryCard libraryCard = new LibraryCard();
            libraryCard.setType(type);
            libraryCard.setCards(collapsedList);
            libraryCard.setCount(typeCount);
            libraryCardList.add(libraryCard);
        });
        int libraryCount = libraryCardList.stream().map(LibraryCard::getCount).reduce(0, Integer::sum);
        library.setCards(libraryCardList);
        library.setCount(libraryCount);
        deck.setLibrary(library);

        List<String> errors = cardCounts.stream().filter(FOUND_CARD.negate()).map(CardCount::getComments).map(String::trim).collect(Collectors.toList());

        Set<String> groups = new HashSet<>();
        boolean hasBannedCards = false;
        for (CardCount cardCount : deck.getCrypt().getCards()) {
            String id = String.valueOf(cardCount.getId());
            CardSummary card = cardSearch.get(id);
            if (!card.getGroup().equalsIgnoreCase("ANY")) {
                groups.add(card.getGroup());
            }
            if (card.isBanned()) {
                hasBannedCards = true;
            }
        }
        for (LibraryCard libraryCard : deck.getLibrary().getCards()) {
            for (CardCount cardCount : libraryCard.getCards()) {
                String id = String.valueOf(cardCount.getId());
                CardSummary card = cardSearch.get(id);
                if (card.isBanned()) {
                    hasBannedCards = true;
                }
            }
        }
        DeckStats stats = new DeckStats(cryptCount, libraryCount, groups, hasBannedCards);
        logger.debug("Parsed deck with {} errors, {} crypt, and {} library", errors.size(), cryptCount, libraryCount);
        return new ExtendedDeck(deck, stats, errors);
    }

    private static Optional<CardCount> parseLine(String deckLine) throws IllegalArgumentException {
        final String cleanLine = sanitizeLine(deckLine);
        Matcher countMatcher = COUNT_PATTERN.matcher(cleanLine);
        if (cleanLine.isEmpty()) {
            logger.debug("Empty line - skipping");
            return Optional.empty();
        }

        Optional<CardSummary> result;
        int count;

        if (countMatcher.find()) {
            count = Integer.parseInt(countMatcher.group(1));
            String cardName = countMatcher.group(2);
            result = cardSearch.findCard(cardName);
        } else {
            count = 1;
            result = cardSearch.findCard(cleanLine);
        }

        CardCount cardCount = result.map(cardEntry -> {
            CardCount found = new CardCount();
            found.setId(Integer.parseInt(cardEntry.getId()));
            found.setName(cardEntry.getName());
            found.setCount(count);
            logger.debug("{} - found {} copies of {}", deckLine, found.getCount(), cardEntry.getName());
            return found;
        }).orElseGet(() -> {
            CardCount error = new CardCount();
            error.setComments(deckLine);
            logger.debug("[{}] can't be mapped to a card", cleanLine);
            return error;
        });
        return Optional.of(cardCount);
    }

    private static String sanitizeLine(String original) {
        return original.trim()
                .toLowerCase()
                .replaceAll(COMMENT_PATTERN.pattern(), "")
                .replaceAll(WINDOWS_QUOTE_PATTERN.pattern(), "'")
                .replaceAll(LEGACY_JOL.pattern(), "")
                .replaceAll(HEADER_PATTERN.pattern(), "")
                .replaceAll(ADVANCED_PATTERN.pattern(), "adv")
                .replaceAll(EXTRA_SPACE_PATTERN.pattern(), " ")
                .replaceAll("pentex subversion", "pentex(tm) subversion")
                .trim();
    }
}
