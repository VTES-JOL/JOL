package net.deckserver.game.json.deck;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.SummaryCard;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Ignore
public class DeckTest {

    private static Pattern countPattern = Pattern.compile("^(\\d+)\\s*[xX]?\\s*([^\\t]+).*$");
    private static Predicate<String> playerMatcher = (name) -> name.matches("^player\\d*");
    private static Predicate<String> deckMatcher = (name) -> name.matches("^deck\\d*");
    private static Path basePath = Paths.get("/home/shannon/data");
    private static ObjectMapper objectMapper;
    private static Map<String, String> cardNameIdMap = new HashMap<>();
    private static Map<String, SummaryCard> cardKeySummaryMap = new HashMap<>();
    private static Predicate<DeckItem> isCard = (item) -> item.getKey() != null;
    private static Predicate<DeckItem> isComment = (item) -> item.getComment() != null && !item.getComment().isEmpty();
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
            card.getNames().stream().map(String::toLowerCase).forEach(name -> cardNameIdMap.put(name, card.getKey()));
            cardKeySummaryMap.put(card.getKey(), card);
        });
        System.out.println("Populated " + cardNameIdMap.size() + " name entries");
    }

    @Test
    public void sampleDeck() throws Exception {
        List<String> deckLines = Files.readAllLines(Paths.get("src/test/resources/player1/deck.txt"));
        Deck deck = parseDeck(deckLines);
        log.info("Parsed deck with " + deckLines.size() + " lines, got " + deck.getCryptCount() + " crypt cards, and " + deck.getLibraryCount() + " library cards. " + deck.getErrors().size() + " errors");
        objectMapper.writeValue(new File("target/deck.json"), deck);
    }

    @Test
    public void convertDecks() throws Exception {
        List<String> players = getPlayerIds();
        for (String player : players) {
            List<String> decks = getPlayerDeckIds(player);
            for (String deck : decks) {
                Path deckPath = basePath.resolve(player).resolve(deck + ".txt");
                List<String> deckLines = null;
                try {
                    deckLines = Files.readAllLines(deckPath, Charset.forName("UTF-8"));
                } catch (MalformedInputException e) {
                    deckLines = Files.readAllLines(deckPath, Charset.forName("ISO8859-1"));
                }
                deckLines = deckLines.stream()
                        .map(s -> s.replaceAll("^Z@.*@Z", ""))
                        .map(s -> s.replaceAll("^ZZZ@@@.*@@@ZZZ", ""))
                        .map(s -> s.replaceAll("^\\s*", ""))
                        .map(s -> s.replaceAll("\\s{2}", "\t"))
                        .collect(Collectors.toList());
                Deck parsedDeck = parseDeck(deckLines);
                Path targetPath = Paths.get("target", player, deck + ".json");
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath.getParent());
                    Files.createFile(targetPath);
                }
                objectMapper.writeValue(targetPath.toFile(), parsedDeck);
                log.info("Parsed {} {}, {} lines with {} crypt, {} library, {} ignored, and {} errors", player, deck, deckLines.size(), parsedDeck.getCryptCount(), parsedDeck.getLibraryCount(), parsedDeck.getIgnored().size(), parsedDeck.getErrors().size());
            }
        }
    }

    private static Properties load(Path propertyPath) {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader(propertyPath.toFile())) {
            properties.load(fileReader);
            return properties;
        } catch (FileNotFoundException e) {
            log.error("Unable to find file {}", propertyPath);
        } catch (IOException e) {
            log.error("Error reading property file {}", propertyPath);
        }
        throw new IllegalArgumentException("Unable to find properties file");
    }

    private static List<String> getPlayerIds() {
        Properties systemProperties = load(basePath.resolve("system.properties"));
        return systemProperties.stringPropertyNames().stream().filter(playerMatcher).collect(Collectors.toList());
    }

    private static List<String> getPlayerDeckIds(String playerId) {
        Properties playerProperties = load(basePath.resolve(playerId).resolve("player.properties"));
        return playerProperties.stringPropertyNames().stream().filter(deckMatcher).collect(Collectors.toList());
    }

    private static Deck loadDeck(String playerId, String deckId) throws IOException {
        try {
            List<String> deckLines = Files.readAllLines(basePath.resolve(playerId).resolve(deckId + ".txt"));
            return parseDeck(deckLines);
        } catch (IOException e) {
            log.error("Error reading {} {}: {}", playerId, deckId, e.getMessage());
            throw e;
        }
    }

    private static Deck parseDeck(List<String> deckLines) {
        Deck deck = new Deck();
        List<String> errors = new ArrayList<>();
        Map<String, DeckItem> itemMap = new HashMap<>();
        List<String> ignored = new ArrayList<>();
        for (String line : deckLines) {
            try {
                Optional<DeckItem> item = parseLine(line);
                item.filter(isCard).ifPresent(card -> {
                    if (itemMap.containsKey(card.getKey())) {
                        DeckItem existing = itemMap.get(card.getKey());
                        log.debug("Updating " + existing.getName() + ", had " + existing.getCount() + " copies, adding " + card.getCount());
                        existing.setCount(existing.getCount() + card.getCount());
                    } else {
                        log.debug("Adding " + card.getCount() + " copies of " + card.getName());
                        itemMap.put(card.getKey(), card);
                    }
                });
                item.filter(isComment).map(DeckItem::getComment).ifPresent(ignored::add);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        Map<String, Integer> contents = new HashMap<>();
        itemMap.values().forEach(item -> contents.put(item.getKey(), item.getCount()));
        int cryptCount = itemMap.values().stream().filter(isCard).filter(isCrypt).mapToInt(DeckItem::getCount).sum();
        int libraryCount = itemMap.values().stream().filter(isCard).filter(isLibrary).mapToInt(DeckItem::getCount).sum();
        deck.setCryptCount(cryptCount);
        deck.setLibraryCount(libraryCount);
        deck.setContents(contents);
        deck.setIgnored(ignored);
        deck.setErrors(errors);
        return deck;
    }

    private static Optional<DeckItem> parseLine(String deckLine) throws IllegalArgumentException {
        final String cleanLine = deckLine.trim();
        Matcher countMatcher = countPattern.matcher(cleanLine);
        if (cleanLine.isEmpty()) {
            return Optional.empty();
        } else if (countMatcher.find()) {
            Integer count = Integer.valueOf(countMatcher.group(1));
            String cardName = countMatcher.group(2);
            return Optional.of(generate(count, cardName));
        }
        // see if line is a just a single card name
        return getKey(cleanLine).map(name -> Optional.of(generate(1, cleanLine)))
                .orElse(Optional.of(DeckItem.of(cleanLine)));
    }

    private static DeckItem generate(Integer count, String cardName) throws IllegalArgumentException {
        return getKey(cardName)
                .map(cardKeySummaryMap::get)
                .map(card -> DeckItem.of(card.getKey(), card.getDisplayName(), count, card.isCrypt()))
                .orElseThrow(() -> new IllegalArgumentException(count + " x " + cardName));
    }

    private static Optional<String> getKey(String cardName) {
        return Optional.ofNullable(cardNameIdMap.get(cardName.toLowerCase()));
    }
}