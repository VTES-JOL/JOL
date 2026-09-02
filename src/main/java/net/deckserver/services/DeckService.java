package net.deckserver.services;

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
import java.util.function.Function;

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
        migrateStorageToV5();
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
     * Persists deck content as raw text, unparsed — used only for a LEGACY deck
     * that still has unresolved card lines (see
     * {@link net.deckserver.JolAdmin#saveLegacyDeckText}). Everything else
     * stores KRCG v5 JSON via {@link #saveDeck}.
     */
    public static void saveRawDeckContent(String deckId, String rawText) {
        instance().requireJpaWriteAlways(em -> deckRepository.saveRawContent(em, deckId, rawText));
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
        Deck canonical = deck != null && deck.getDeck() != null ? deck.getDeck() : new Deck();
        return KrcgV5Mapper.toJson(canonical);
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

    /**
     * One-time migration of every stored deck to the KRCG v5 JSON shape
     * ({@link KrcgV5Mapper}). Idempotent — a row already in v5 form is skipped
     * before any parsing.
     *
     * <ul>
     *   <li><b>MODERN / TAGGED player decks</b> and all <b>registration</b> /
     *       <b>tournament</b> deck snapshots: re-serialised to v5 (shape change
     *       only, they always parse).</li>
     *   <li><b>LEGACY player decks</b>: converted to v5 <em>and</em> promoted to
     *       TAGGED only when the raw text parses with zero unresolved cards;
     *       otherwise the raw text is left untouched (card names have drifted —
     *       the owner re-saves it through the editor). See the editor's
     *       raw-text mode.</li>
     * </ul>
     */
    private void migrateStorageToV5() {
        migratePlayerDecksToV5();
        migrateSnapshotDeckContentToV5(
                "SELECT r.id FROM RegistrationEntity r WHERE r.deckContent IS NOT NULL AND r.deckContent <> ''",
                net.deckserver.jpa.entity.RegistrationEntity.class,
                net.deckserver.jpa.entity.RegistrationEntity::getDeckContent,
                net.deckserver.jpa.entity.RegistrationEntity::setDeckContent,
                "registration");
        migrateSnapshotDeckContentToV5(
                "SELECT r.id FROM TournamentRegistrationEntity r WHERE r.deckContent IS NOT NULL AND r.deckContent <> ''",
                net.deckserver.jpa.entity.TournamentRegistrationEntity.class,
                net.deckserver.jpa.entity.TournamentRegistrationEntity::getDeckContent,
                net.deckserver.jpa.entity.TournamentRegistrationEntity::setDeckContent,
                "tournament registration");
    }

    /**
     * One row = one transaction: a single unconvertible deck logs and is skipped
     * rather than aborting startup, and a fresh boot only revisits the rows still
     * not in v5 form (LEGACY decks with unresolved cards, plus any that errored).
     */
    private void migratePlayerDecksToV5() {
        java.util.List<String> deckIds = jpaRead(em -> em.createQuery(
                "SELECT d.deckId FROM DeckInfoEntity d", String.class).getResultList());

        int[] tally = {0, 0, 0, 0}; // converted-legacy, reshaped, kept-legacy, failed
        Map<String, Deck> promotedFromLegacy = new java.util.LinkedHashMap<>();

        for (String deckId : deckIds) {
            boolean ok = jpaWriteAlways(em -> {
                DeckInfoEntity entity = em.createQuery(
                                "SELECT d FROM DeckInfoEntity d JOIN FETCH d.player WHERE d.deckId = :id", DeckInfoEntity.class)
                        .setParameter("id", deckId).getResultStream().findFirst().orElse(null);
                if (entity == null) {
                    return;
                }
                String raw = deckRepository.findRawContent(em, deckId);
                if (raw == null || raw.isBlank() || KrcgV5Mapper.looksLikeV5(raw.strip())) {
                    return;
                }
                boolean legacy = entity.getFormat() == DeckFormat.LEGACY;

                // Raw-text LEGACY decks must be parsed directly — DeckNormalizer
                // drops unresolved lines (they never enter the Deck structure), so
                // analyze() on its output would report zero errors and we'd
                // silently convert a deck with a dropped card. parseDeck keeps the
                // unresolved lines as errors.
                boolean rawText = !raw.strip().startsWith("{");
                ExtendedDeck ed = legacy && rawText
                        ? DeckParser.parseDeck(raw)
                        : DeckParser.analyze(DeckNormalizer.normalize(raw));

                if (legacy && !ed.getErrors().isEmpty()) {
                    tally[2]++;
                    logger.debug("Keeping deck {} as LEGACY raw text ({} unresolved lines)", deckId, ed.getErrors().size());
                    return;
                }
                deckRepository.saveRawContent(em, deckId, KrcgV5Mapper.toJson(ed.getDeck()));
                if (legacy) {
                    Set<String> tags = ValidatorFactory.getTags(ed.getDeck());
                    DeckInfo info = entity.toDeckInfo();
                    info.setGameFormats(tags);
                    info.setFormat(DeckFormat.TAGGED);
                    deckRepository.saveDeckInfo(em, entity.getPlayerName(), info.getDeckName(), info);
                    promotedFromLegacy.put(deckId, ed.getDeck());
                    tally[0]++;
                } else {
                    tally[1]++;
                }
            });
            if (!ok) {
                tally[3]++;
                logger.warn("Deck {} could not be migrated to v5 — left as-is, will retry next boot", deckId);
            }
        }

        // Recompute per-format validity for the promoted decks (own transaction each).
        promotedFromLegacy.forEach(DeckValidityService::computeAndPersist);
        if (tally[0] + tally[1] + tally[3] > 0) {
            logger.info("Deck storage → v5: {} LEGACY converted to TAGGED, {} reshaped, {} kept as LEGACY raw text, {} failed",
                    tally[0], tally[1], tally[2], tally[3]);
        }
    }

    private <T> void migrateSnapshotDeckContentToV5(String idJpql, Class<T> type,
                                                    Function<T, String> getter, java.util.function.BiConsumer<T, String> setter,
                                                    String label) {
        java.util.List<?> ids = jpaRead(em -> em.createQuery(idJpql).getResultList());
        int[] tally = {0, 0}; // reshaped, failed
        for (Object id : ids) {
            boolean ok = jpaWriteAlways(em -> {
                T row = em.find(type, id);
                if (row == null) {
                    return;
                }
                String raw = getter.apply(row);
                if (raw == null || raw.isBlank() || KrcgV5Mapper.looksLikeV5(raw.strip())) {
                    return;
                }
                setter.accept(row, KrcgV5Mapper.toJson(DeckNormalizer.normalize(raw)));
                tally[0]++;
            });
            if (!ok) {
                tally[1]++;
            }
        }
        if (tally[0] + tally[1] > 0) {
            logger.info("Deck storage → v5: {} {} deck snapshots reshaped, {} failed", tally[0], label, tally[1]);
        }
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
