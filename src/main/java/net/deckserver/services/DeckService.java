package net.deckserver.services;

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
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DeckService extends PersistedService {

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);
    private static final DeckRepository deckRepository = new DeckRepository();
    private static final RegistrationRepository registrationRepository = new RegistrationRepository();
    private static final DeckService INSTANCE = new DeckService();

    private DeckService() {
        super("DeckService", 0);
        upgrade();
    }

    public static DeckInfo get(String playerName, String deckName) {
        return INSTANCE.jpaRead(em -> deckRepository.findByPlayerAndName(em, playerName, deckName));
    }

    public static void addDeck(String playerName, String deckName, DeckInfo deckInfo) {
        INSTANCE.jpaWrite(em -> deckRepository.saveDeckInfo(em, playerName, deckName, deckInfo));
    }

    public static void remove(String playerName, String deckName) {
        INSTANCE.jpaWrite(em -> deckRepository.delete(em, playerName, deckName));
    }

    public static Set<String> getPlayerDeckNames(String playerName) {
        return INSTANCE.jpaRead(em -> deckRepository.findByPlayerName(em, playerName).keySet());
    }

    public static Map<String, DeckInfo> getPlayerDecks(String playerName) {
        return INSTANCE.jpaRead(em -> deckRepository.findByPlayerName(em, playerName));
    }

    public static ExtendedDeck getDeck(String deckId) {
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
        // legacy decks are migrated from decks/<id>.txt as raw text, not ExtendedDeck JSON
        try (EntityManager em = JpaFactory.createEntityManager()) {
            String content = deckRepository.findRawContent(em, deckId);
            if (content != null) {
                return content;
            }
        }
        throw new IOException("No legacy deck content found for " + deckId);
    }

    public static ExtendedDeck getGameDeck(String gameId, String deckId) {
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
        // deck content is stored in registration.deck_content at registration time
        return true;
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    private void upgrade() {
        // Migrate any MODERN decks with no game-format tags to TAGGED format.
        // Runs at startup; results are persisted immediately so the query returns
        // nothing on subsequent boots.
        jpaWrite(em -> {
            em.createQuery(
                            "SELECT d FROM DeckInfoEntity d JOIN FETCH d.player " +
                            "WHERE d.format = :format AND d.gameFormats IS EMPTY",
                            net.deckserver.jpa.entity.DeckInfoEntity.class)
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
                    });
        });
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
