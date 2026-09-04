package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.validators.ValidationResult;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.jpa.repository.DeckFormatValidityRepository;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.DeckValidity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Owns the {@code deck_format_validity} table: recomputes a deck's per-format
 * validation results whenever its content changes ({@link #computeAndPersist})
 * and serves the cached outcome to the deck editor ({@link #getValidity}).
 *
 * <p>Cache-less like {@link DeckService} — every read goes straight to JPA.
 * The formats checked are the three a player can register a game in
 * ({@code STANDARD}, {@code DUEL}, {@code V5}).
 */
@Singleton
@Startup
public class DeckValidityService extends PersistedService {

    private static final DeckFormatValidityRepository repository = new DeckFormatValidityRepository();

    static final List<GameFormat> VALIDATED_FORMATS = List.of(GameFormat.STANDARD, GameFormat.DUEL, GameFormat.V5);

    private static DeckValidityService instance() {
        return resolve(DeckValidityService.class, DeckValidityService::new);
    }

    DeckValidityService() {
        super("DeckValidityService", 0);
    }

    /**
     * Validates {@code deck} against every {@link #VALIDATED_FORMATS format} and
     * upserts one row per format. Call after persisting the deck's content.
     */
    public static void computeAndPersist(String deckId, Deck deck) {
        if (deckId == null || deck == null) {
            return;
        }
        OffsetDateTime computedAt = OffsetDateTime.now();
        instance().requireJpaWriteAlways(em -> {
            for (GameFormat format : VALIDATED_FORMATS) {
                ValidationResult result = ValidatorFactory.getDeckValidator(format).validate(deck);
                repository.upsert(em, deckId, format, result.isValid(), result.getErrors(), computedAt);
            }
        });
    }

    /** All stored format outcomes for a deck; empty map if never validated. */
    public static Map<GameFormat, DeckValidity> getValidity(String deckId) {
        return instance().jpaRead(em -> repository.findByDeck(em, deckId));
    }

    public static Optional<DeckValidity> getValidity(String deckId, GameFormat format) {
        return instance().jpaRead(em -> repository.findByDeckAndFormat(em, deckId, format));
    }

    public static PersistedService getInstance() {
        return instance();
    }

    @Override
    protected void persist() {
        // write-through only, see computeAndPersist()
    }

    @Override
    protected void load() {
        // no cache — reads go directly to JPA
    }
}
