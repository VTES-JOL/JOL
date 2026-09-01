package net.deckserver.game.cards;

import net.deckserver.game.enums.CardType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A single VTES card, parsed from the VEKN CSV data by {@link CardRegistry}.
 *
 * <p>This is the one canonical card model. {@link CardRegistry} is the single
 * façade over it: loading, id + name resolution, and the small projections
 * some callers want ({@link CardRef} for chat card-links, {@link RegistryStatus}
 * for the reload endpoint). The deck editor and game board consume it through
 * their existing REST beans ({@code CardDetailBean}, {@code CardSnapshot}).
 */
public sealed interface Card permits CryptCard, LibraryCard {

    String id();

    /** The printed name, exactly as it appears on the card. */
    String name();

    /**
     * Display name: the printed name for a library card, the printed name plus
     * a {@code " (G# ADV)"} qualifier for a crypt card.
     */
    String displayName();

    /** Alternate / printed names ("Aka" column), never null. */
    List<String> aka();

    /** Set abbreviations the card was printed in, uppercased, never null. */
    List<String> sets();

    String cardText();

    String artist();

    boolean banned();

    /** True for cards imported from the playtest CSVs (the old "secured" set). */
    boolean playtest();

    /** False only for cards whose text explicitly makes them non-unique. */
    boolean unique();

    /** The broad {@link CardType} — VAMPIRE / IMBUED for crypt, else the first library type. */
    CardType cardType();

    default boolean isCrypt() {
        return this instanceof CryptCard;
    }

    /** Vampires carry blood. */
    default boolean hasBlood() {
        return cardType() == CardType.VAMPIRE;
    }

    /** Allies, retainers and imbued carry life. */
    default boolean hasLife() {
        return CardType.lifeTypes().contains(cardType());
    }

    /** A minion is anything that sits on the table carrying blood or life. */
    default boolean isMinion() {
        return hasBlood() || hasLife();
    }

    /** Raw "/"-joined type string, e.g. {@code "Action/Combat"} or {@code "Vampire"}. */
    default String typeLine() {
        return switch (this) {
            case CryptCard c -> c.type() == CryptType.IMBUED ? "Imbued" : "Vampire";
            case LibraryCard l -> String.join("/", l.types());
        };
    }

    /** Space-joined, underscored, sorted lowercase type tokens — a CSS class list. */
    default String typeClass() {
        return Arrays.stream(typeLine().toLowerCase().trim().split("/"))
                .map(s -> s.replace(' ', '_'))
                .sorted()
                .collect(Collectors.joining(" "));
    }

    /** Underscored, sorted lowercase clan tokens — a CSS class list. */
    default List<String> clanClasses() {
        List<String> clans = switch (this) {
            case CryptCard c -> c.clan() == null ? List.of() : List.of(c.clan());
            case LibraryCard l -> l.requirementClans();
        };
        return clans.stream()
                .map(s -> s.replace(' ', '_').toLowerCase())
                .sorted()
                .collect(Collectors.toList());
    }
}
