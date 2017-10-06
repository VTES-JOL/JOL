package net.deckserver.storage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.SummaryCard;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeckTest {

    private static Pattern countPattern = Pattern.compile("^(\\d+)\\s*[xX]?\\s*([^\\t]+).*$");
    private static ObjectMapper objectMapper;
    private static Map<String, String> cardNameIdMap = new HashMap<>();
    private static Map<String, SummaryCard> cardKeySummaryMap = new HashMap<>();
    private static Predicate<DeckItem> isCard = (item) -> item.getKey() != null;
    private static Predicate<DeckItem> isCrypt = DeckItem::isCrypt;
    private static Predicate<DeckItem> isLibrary = (item) -> !item.isCrypt();

    @BeforeClass
    public static void init() throws IOException {
        Path cardPath = Paths.get("src/test/resources/cards/summary.json");
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        JavaType summaryCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, SummaryCard.class);
        List<SummaryCard> cards = objectMapper.readValue(cardPath.toFile(), summaryCollectionType);
        System.out.println("Loaded " + cards.size() + " cards");
        cards.forEach(card -> {
            card.getNames().forEach(name -> cardNameIdMap.put(name, card.getKey()));
            cardKeySummaryMap.put(card.getKey(), card);
        });
        System.out.println("Populated " + cardNameIdMap.size() + " name entries");
    }

    @Test
    public void sampleDeck() throws Exception {
        List<String> deckLines = Files.readAllLines(Paths.get("src/test/resources/player1/deck.txt"));
        Deck deck = parseDeck(deckLines);
        System.out.println("Parsed deck with " + deckLines.size() + " lines, got " + deck.getCryptCount() + " crypt cards, and " + deck.getLibraryCount() + " library cards. " + deck.getErrors().size() + " errors");
        objectMapper.writeValue(new File("target/deck.json"), deck);
    }

    private static Deck parseDeck(List<String> deckLines) {
        Deck deck = new Deck();
        List<String> errors = new ArrayList<>();
        Map<String, DeckItem> itemMap = new HashMap<>();
        for (String line : deckLines) {
            try {
                Optional<DeckItem> item = parseLine(line);
                item.filter(isCard).ifPresent(card -> {
                    if (itemMap.containsKey(card.getKey())) {
                        DeckItem existing = itemMap.get(card.getKey());
                        System.out.println("Updating " + existing.getName() + ", had " + existing.getCount() + " copies, adding " + card.getCount());
                        existing.setCount(existing.getCount() + card.getCount());
                    } else {
                        System.out.println("Adding " + card.getCount() + " copies of " + card.getName());
                        itemMap.put(card.getKey(), card);
                    }
                });
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        List<DeckItem> items = new ArrayList<>(itemMap.values());
        int cryptCount = items.stream().filter(isCard).filter(isCrypt).mapToInt(DeckItem::getCount).sum();
        int libraryCount = items.stream().filter(isCard).filter(isLibrary).mapToInt(DeckItem::getCount).sum();
        deck.setCryptCount(cryptCount);
        deck.setLibraryCount(libraryCount);
        deck.setContents(items);
        deck.setErrors(errors);
        return deck;
    }

    private static Optional<DeckItem> parseLine(String deckLine) throws IllegalArgumentException {
        deckLine = deckLine.trim();
        Matcher countMatcher = countPattern.matcher(deckLine);
        if (deckLine.isEmpty()) {
            return Optional.empty();
        } else if (countMatcher.find()) {
            Integer count = Integer.valueOf(countMatcher.group(1));
            String cardName = countMatcher.group(2);
            return Optional.of(generate(count, cardName));
        } else {
            return Optional.of(DeckItem.of(deckLine));
        }
    }

    private static DeckItem generate(Integer count, String cardName) throws IllegalArgumentException {
        String cardKey = cardNameIdMap.get(cardName);
        if (cardKey == null) {
            throw new IllegalArgumentException(cardName);
        }
        SummaryCard summaryCard = cardKeySummaryMap.get(cardKey);
        return DeckItem.of(cardKey, summaryCard.getDisplayName(), count, summaryCard.isCrypt());
    }
}