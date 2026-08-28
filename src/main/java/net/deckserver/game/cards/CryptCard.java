package net.deckserver.game.cards;

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
 * New Blood). {@code path} is the V5 blood path, or null.
 */
public record CryptCard(
        String id,
        String name,
        List<String> aka,
        List<String> sets,
        String cardText,
        String artist,
        boolean banned,
        CryptType type,
        String clan,
        String path,
        String group,
        boolean advanced,
        int capacity,
        List<String> disciplines,
        String title
) implements Card {
}
