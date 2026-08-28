package net.deckserver.storage.json.deck;

import net.deckserver.game.enums.GameFormat;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A deck's validation outcome for one game format, as stored in
 * {@code deck_format_validity} and served to the deck editor. {@code errors}
 * is empty when {@code valid}.
 */
public record DeckValidity(GameFormat format, boolean valid, List<String> errors, OffsetDateTime computedAt) {
}
