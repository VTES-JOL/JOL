package net.deckserver.game.cards;

import net.deckserver.game.enums.CardType;

import java.util.List;

/**
 * A crypt card — a Vampire or an Imbued.
 *
 * <p>{@code disciplines} holds space-separated codes preserving case:
 * lowercase = inferior (e.g. {@code "ani"}), UPPERCASE = superior (e.g.
 * {@code "ANI"}). For Imbued cards these are virtue codes (mar, inn, jud, def,
 * red, viz, ven).
 *
 * <p>{@code clan} is the vampire clan or the Imbued creed (Martyr, Judge, …).
 * {@code group} is {@code "1"}–{@code "7"} or {@code "ANY"} (Anarch Convert,
 * New Blood). {@code path} is the V5 blood path, or null. {@code sect} is the
 * card's intrinsic starting sect, derived from clan + card text.
 * {@code votes} is the title's vote count ({@code "1"}–{@code "4"} or
 * {@code "P"} for priscus), or {@code ""}.
 */
public record CryptCard(
        String id,
        String name,
        List<String> aka,
        List<String> sets,
        String cardText,
        String artist,
        boolean banned,
        boolean playtest,
        boolean unique,
        CryptType type,
        String clan,
        String sect,
        String path,
        String group,
        boolean advanced,
        boolean infernal,
        int capacity,
        List<String> disciplines,
        String title,
        String votes
) implements Card {

    @Override
    public CardType cardType() {
        return type == CryptType.IMBUED ? CardType.IMBUED : CardType.VAMPIRE;
    }

    @Override
    public String displayName() {
        return name + cryptSuffix();
    }

    /** The {@code " (G# ADV)"} qualifier appended to the printed name. */
    public String cryptSuffix() {
        if ("ANY".equals(group)) {
            return advanced ? " (ADV)" : "";
        }
        return " (G" + group + (advanced ? " ADV" : "") + ")";
    }
}
