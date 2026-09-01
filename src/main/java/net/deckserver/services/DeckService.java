package net.deckserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.entity.DeckInfoEntity;
import net.deckserver.jpa.repository.DeckRepository;
import net.deckserver.storage.json.deck.*;
import net.deckserver.storage.json.system.DeckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
@Startup
public class DeckService extends PersistedService {

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);
    private static final DeckRepository deckRepository = new DeckRepository();

    private static DeckService instance() {
        return resolve(DeckService.class, DeckService::new);
    }

    DeckService() {
        super("DeckService", 0);
        upgrade();
    }

    public static DeckInfo get(String playerName, String deckName) {
        return instance().jpaRead(em -> deckRepository.findByPlayerAndName(em, playerName, deckName));
    }

    public static void addDeck(String playerName, String deckName, DeckInfo deckInfo) {
        instance().requireJpaWriteAlways(em -> deckRepository.saveDeckInfo(em, playerName, deckName, deckInfo));
    }

    public static void remove(String playerName, String deckName) {
        instance().requireJpaWriteAlways(em -> deckRepository.delete(em, playerName, deckName));
    }

    public static Set<String> getPlayerDeckNames(String playerName) {
        return instance().jpaRead(em -> deckRepository.findByPlayerName(em, playerName).keySet());
    }

    public static Map<String, DeckInfo> getPlayerDecks(String playerName) {
        return instance().jpaRead(em -> deckRepository.findByPlayerName(em, playerName));
    }

    public static ExtendedDeck getDeck(String deckId) {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            return deckRepository.findContent(em, deckId);
        } catch (Exception e) {
            logger.error("JPA read failed for deck {}", deckId, e);
            return new ExtendedDeck();
        }
    }

    public static String getDeckContents(String deckId) {
        ExtendedDeck deck = getDeck(deckId);
        StringBuilder builder = new StringBuilder();
        Consumer<CardCount> itemBuilder = cardCount -> builder.append(cardCount.getCount()).append(" x ").append(cardCount.getName()).append("\n");
        deck.getDeck().getCrypt().getCards().forEach(itemBuilder);
        builder.append("\n");
        deck.getDeck().getLibrary().getCards().forEach(libraryCard -> libraryCard.getCards().forEach(itemBuilder));
        return builder.toString();
    }

    public static String getLegacyContents(String deckId) throws IOException {
        // legacy decks are migrated from decks/<id>.txt as raw text, not ExtendedDeck JSON
        try (EntityManager em = JpaFactory.createEntityManager()) {
            String content = deckRepository.findRawContent(em, deckId);
            if (content != null) {
                return content;
            }
        }
        throw new IOException("No legacy deck content found for " + deckId);
    }

    public static String getDeckComments(String playerName, String deckName) {
        DeckInfo deckInfo = get(playerName, deckName);
        if (deckInfo == null) {
            return "";
        }
        String comments = getDeck(deckInfo.getDeckId()).getDeck().getComments();
        return comments == null ? "" : comments;
    }

    public static void saveDeck(String deckId, ExtendedDeck deck) {
        instance().requireJpaWriteAlways(em -> deckRepository.saveContent(em, deckId, deck));
    }

    /**
     * Serializes a deck for storage as a game registration's frozen snapshot
     * (see {@link RegistrationService#registerDeck}) — the player's own decks/<id>
     * row can keep changing after registration, so the game keeps its own copy.
     *
     * <p>Only the canonical {@link Deck} is written; derived stats/errors are
     * recomputed by {@link #deserializeDeck(String)} on read.
     */
    public static String serializeDeck(ExtendedDeck deck) {
        try {
            Deck canonical = deck != null && deck.getDeck() != null ? deck.getDeck() : new Deck();
            return objectMapper.writeValueAsString(canonical);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize deck for game registration", e);
            return null;
        }
    }

    public static ExtendedDeck deserializeDeck(String json) {
        if (json == null || json.isBlank()) {
            return new ExtendedDeck();
        }
        // DeckNormalizer handles both the current bare-Deck JSON and the old
        // {"deck":…} ExtendedDeck snapshots still held by pre-existing games.
        return DeckParser.analyze(DeckNormalizer.normalize(json));
    }

    public static PersistedService getInstance() {
        return instance();
    }

    private void upgrade() {
        // Migrate any MODERN decks with no game-format tags to TAGGED format.
        // Runs at startup; results are persisted immediately so the query returns
        // nothing on subsequent boots.
        requireJpaWriteAlways(em -> em.createQuery(
                        "SELECT d FROM DeckInfoEntity d JOIN FETCH d.player " +
                                "WHERE d.format = :format AND d.gameFormats IS EMPTY",
                        DeckInfoEntity.class)
                .setParameter("format", DeckFormat.MODERN)
                .getResultList()
                .forEach(entity -> {
                    ExtendedDeck deck = deckRepository.findContent(em, entity.getDeckId());
                    Set<String> tags = ValidatorFactory.getTags(deck.getDeck());
                    DeckInfo info = entity.toDeckInfo();
                    info.setGameFormats(tags);
                    info.setFormat(DeckFormat.TAGGED);
                    logger.info("Upgrading {} to TAGGED with {} tags", entity.getDeckId(), tags);
                    deckRepository.saveDeckInfo(em, entity.getPlayerName(), info.getDeckName(), info);
                }));
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
    }

    @Override
    protected void load() {
        // no startup load needed — reads go directly to JPA
    }
}
