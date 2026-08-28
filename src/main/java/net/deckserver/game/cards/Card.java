package net.deckserver.game.cards;

import java.util.List;

/**
 * A single VTES card, parsed from the VEKN CSV data by {@link CardRegistry}.
 *
 * <p>This is the structured card model ported from the jol-quarkus rewrite. It
 * runs <em>alongside</em> the existing {@link net.deckserver.services.CardService}
 * / {@code CardSummary} (which stays the source for the game board, chat
 * card-links and {@code DeckParser}) — this model adds the parsed fields those
 * lack: split and/or disciplines, structured pool/blood/conviction costs,
 * requirement clans and path, crypt group and capacity. Nothing consumes it
 * yet; it exists to back the deck-editor work (analytics, autocomplete DTOs,
 * KRCG import resolution) landing later.
 */
public sealed interface Card permits CryptCard, LibraryCard {
    String id();

    String name();

    /** Alternate / printed names ("Aka" column), never null. */
    List<String> aka();

    /** Set abbreviations the card was printed in, uppercased, never null. */
    List<String> sets();

    String cardText();

    String artist();

    boolean banned();

    default boolean isCrypt() {
        return this instanceof CryptCard;
    }
}
