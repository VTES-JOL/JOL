package net.deckserver.services;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import jakarta.persistence.EntityManager;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.DeckRepository;
import net.deckserver.jpa.repository.RegistrationRepository;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.DeckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final DeckRepository deckRepository = new DeckRepository();
    private static final RegistrationRepository registrationRepository = new RegistrationRepository();
    private static final DeckService INSTANCE = new DeckService();

    private final Table<String, String, DeckInfo> decks = HashBasedTable.create();

    private DeckService() {
        super("DeckService", 5);
        load();
        upgrade();
    }

    public static DeckInfo get(String playerName, String deckName) {
        return INSTANCE.decks.get(playerName, deckName);
    }

    public static void addDeck(String playerName, String deckName, DeckInfo deckInfo) {
        INSTANCE.decks.put(playerName, deckName, deckInfo);
        INSTANCE.jpaWrite(em -> deckRepository.saveDeckInfo(em, playerName, deckName, deckInfo));
    }

    public static void remove(String playerName, String deckName) {
        INSTANCE.decks.remove(playerName, deckName);
        INSTANCE.jpaWrite(em -> deckRepository.delete(em, playerName, deckName));
    }

    public static Set<String> getPlayerDeckNames(String playerName) {
        return INSTANCE.decks.row(playerName).keySet();
    }

    public static Map<String, DeckInfo> getPlayerDecks(String playerName) {
        return INSTANCE.decks.row(playerName);
    }

    public static ExtendedDeck getDeck(String deckId) {
        if (INSTANCE.testModeEnabled) {
            Path deckPath = DataPaths.path("decks", deckId + ".json");
            try {
                return objectMapper.readValue(deckPath.toFile(), ExtendedDeck.class);
            } catch (IOException e) {
                return new ExtendedDeck();
            }
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            return deckRepository.findContent(em, deckId);
        } catch (Exception e) {
            logger.error("JPA read failed for deck {}", deckId, e);
            return new ExtendedDeck();
        }
    }

    public static String getDeckContents(String deckId) throws IOException {
        ExtendedDeck deck = getDeck(deckId);
        StringBuilder builder = new StringBuilder();
        Consumer<CardCount> itemBuilder = cardCount -> builder.append(cardCount.getCount()).append(" x ").append(cardCount.getName()).append("\n");
        deck.getDeck().getCrypt().getCards().forEach(itemBuilder);
        builder.append("\n");
        deck.getDeck().getLibrary().getCards().forEach(libraryCard -> libraryCard.getCards().forEach(itemBuilder));
        return builder.toString();
    }

    public static String getLegacyContents(String deckId) throws IOException {
        Path deckPath = DataPaths.path("decks", deckId + ".txt");
        return Files.readString(deckPath);
    }

    public static ExtendedDeck getGameDeck(String gameId, String deckId) {
        if (INSTANCE.testModeEnabled) {
            Path gameDeckPath = DataPaths.path("games", gameId, deckId + ".json");
            try {
                return objectMapper.readValue(gameDeckPath.toFile(), ExtendedDeck.class);
            } catch (IOException e) {
                return new ExtendedDeck();
            }
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            var entity = registrationRepository.findByGameAndDeck(em, gameId, deckId);
            if (entity != null && entity.getDeckContent() != null) {
                return objectMapper.readValue(entity.getDeckContent(), ExtendedDeck.class);
            }
        } catch (Exception e) {
            logger.error("Failed to load game deck {}/{}", gameId, deckId, e);
        }
        return new ExtendedDeck();
    }

    public static String getDeckComments(String playerName, String deckName) {
        String comments = getDeck(get(playerName, deckName).getDeckId()).getDeck().getComments();
        return comments == null ? "" : comments;
    }

    public static void saveDeck(String deckId, ExtendedDeck deck) {
        INSTANCE.jpaWrite(em -> deckRepository.saveContent(em, deckId, deck));
    }

    public static boolean copyDeck(String deckId, String gameId) {
        // deck content is stored in jol_registration.deck_content at registration time
        return true;
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
        jpaWrite(em -> decks.cellSet().forEach(cell ->
                deckRepository.saveDeckInfo(em, cell.getRowKey(), cell.getColumnKey(), cell.getValue())));
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
    }

    @Override
    protected void load() {
        if (testModeEnabled) {
            loadFromFile();
            return;
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            deckRepository.findAllDeckInfos(em).forEach(entity ->
                    decks.put(entity.getPlayerName(), entity.getId().getDeckName(), entity.toDeckInfo()));
            logger.info("Loaded {} decks from JPA", decks.size());
        } catch (Exception e) {
            logger.error("JPA load failed for DeckService", e);
        }
    }

    private void loadFromFile() {
        Path path = DataPaths.path("decks.json");
        if (!Files.exists(path)) return;
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType deckMapType = typeFactory.constructMapType(Map.class, String.class, DeckInfo.class);
            Map<String, Map<String, DeckInfo>> map = objectMapper.readValue(path.toFile(),
                    typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), deckMapType));
            map.forEach((playerName, decksMap) ->
                    decksMap.forEach((deckName, deckInfo) -> decks.put(playerName, deckName, deckInfo)));
            logger.info("Loaded {} decks from file", decks.size());
        } catch (IOException e) {
            logger.error("Unable to load decks from file", e);
        }
    }

    private void jpaWrite(java.util.function.Consumer<EntityManager> action) {
        if (testModeEnabled) return;
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for DeckService", e);
        }
    }
}
