package net.deckserver.services;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.DeckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DeckService extends PersistedService {

    public static final Predicate<DeckInfo> MODERN_DECK = info -> DeckFormat.MODERN.equals(info.getFormat());
    public static final Predicate<DeckInfo> NO_TAGS = info -> info.getGameFormats().isEmpty();
    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "decks.json");
    private static final DeckService INSTANCE = new DeckService();

    private final Table<String, String, DeckInfo> decks = HashBasedTable.create();

    private DeckService() {
        super("DeckService", 5);
        load();
        upgrade();
    }

    public static  DeckInfo get(String playerName, String deckName) {
        return INSTANCE.decks.get(playerName, deckName);
    }

    public static  void addDeck(String playerName, String deckName, DeckInfo deckInfo) {
        INSTANCE.decks.put(playerName, deckName, deckInfo);
    }

    public static  void remove(String playerName, String deckName) {
        INSTANCE.decks.remove(playerName, deckName);
    }

    public static  Set<String> getPlayerDeckNames(String playerName) {
        return INSTANCE.decks.row(playerName).keySet();
    }

    public static  Map<String, DeckInfo> getPlayerDecks(String playerName) {
        return INSTANCE.decks.row(playerName);
    }

    public static  ExtendedDeck getDeck(String deckId) {
        String deckString = String.format("decks/%s.json", deckId);
        Path deckPath = Paths.get(System.getenv("JOL_DATA"), deckString);
        try {
            return objectMapper.readValue(deckPath.toFile(), ExtendedDeck.class);
        } catch (IOException e) {
            return new ExtendedDeck();
        }
    }

    public static  String getDeckContents(String deckId) throws IOException {
        ExtendedDeck deck = getDeck(deckId);
        StringBuilder builder = new StringBuilder();
        Consumer<CardCount> itemBuilder = cardCount -> builder.append(cardCount.getCount()).append(" x ").append(cardCount.getName()).append("\n");
        deck.getDeck().getCrypt().getCards().forEach(itemBuilder);
        builder.append("\n");
        deck.getDeck().getLibrary().getCards().forEach(libraryCard -> libraryCard.getCards().forEach(itemBuilder));
        return builder.toString();
    }

    public static  String getLegacyContents(String deckId) throws IOException {
        Path deckPath = Paths.get(System.getenv("JOL_DATA"), "decks", deckId + ".txt");
        return Files.readString(deckPath);
    }

    public static  ExtendedDeck getGameDeck(String gameId, String deckId) {
        try {
            Path gameDeckPath = Paths.get(System.getenv("JOL_DATA"), String.format("games/%s/%s.json", gameId, deckId));
            return objectMapper.readValue(gameDeckPath.toFile(), ExtendedDeck.class);
        } catch (IOException e) {
            return new ExtendedDeck();
        }
    }

    public static  void saveDeck(String deckId, ExtendedDeck deck) {
        try {
            Path deckPath = Paths.get(System.getenv("JOL_DATA"), "decks", deckId + ".json");
            objectMapper.writeValue(deckPath.toFile(), deck);
        } catch (IOException e) {
            logger.error("Unable to save deck {}", deckId, e);
        }
    }

    public static  boolean copyDeck(String deckId, String gameId) {
        try {
            Path deckPath = Paths.get(System.getenv("JOL_DATA"), "decks", deckId + ".json");
            Path gamePath = Paths.get(System.getenv("JOL_DATA"), "games", gameId, deckId + ".json");
            logger.debug("Copying {} to {}", deckPath, gamePath);
            Files.copy(deckPath, gamePath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            logger.error("Unable to load deck for {}", deckId, e);
            return false;
        }

    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    private void upgrade() {
        decks.values().stream()
                .filter(MODERN_DECK)
                .filter(NO_TAGS)
                .filter(Objects::nonNull)
                .forEach(deckInfo -> {
                    ExtendedDeck deck = getDeck(deckInfo.getDeckId());
                    Set<String> tags = ValidatorFactory.getTags(deck.getDeck());
                    deckInfo.setGameFormats(tags);
                    deckInfo.setFormat(DeckFormat.TAGGED);
                    logger.info("Upgrading {} to {} with {} tags", deckInfo.getDeckId(), deckInfo.getFormat(), tags);
                });
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} decks", decks.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), decks);
            logger.debug("Successfully persisted decks");
        } catch (IOException e) {
            logger.error("Unable to save decks", e);
        }
    }

    @Override
    protected void load() {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing decks file found");
            return;
        }

        try {
            MapType deckMapType = typeFactory.constructMapType(Map.class, String.class, DeckInfo.class);
            Map<String, Map<String, DeckInfo>> decksMapFile = objectMapper.readValue(PERSISTENCE_PATH.toFile(), typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), deckMapType));
            decksMapFile.forEach((playerName, decksMap) -> {
                decksMap.forEach((deckName, deckInfo) -> decks.put(playerName, deckName, deckInfo));
            });
            logger.info("Loaded {} decks", decks.size());
        } catch (IOException e) {
            logger.error("Unable to decks", e);
        }

    }
}
