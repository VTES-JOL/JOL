package net.deckserver.rest.bean;

import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CryptCard;
import net.deckserver.game.cards.LibraryCard;

import java.util.List;

/**
 * The full structured card record, projected from {@link Card} /
 * {@link CryptCard} / {@link LibraryCard}, served by
 * {@code GET /jol/api/cards/{id}} (and the batch {@code ?ids=}) for the
 * text-only card mode (brief 6c) — clan, capacity, disciplines, type, cost and
 * card text without an image.
 *
 * <p>This is deliberately NOT {@code CardSnapshot} (per-game, per-viewer, on the
 * refetch hot path) nor {@code CardDetailBean} (deck-editor projection, no card
 * text). Card data is immutable at runtime (only {@code POST
 * /admin/cards/reload} changes it), so responses carry a long cache lifetime.
 *
 * <p>{@code crypt} discriminates: crypt-only fields ({@code clan}..{@code votes})
 * are null on a library card, library-only fields
 * ({@code flavorText}..{@code doNotReplace}) are null on a crypt card.
 * {@code disciplines} preserves case (UPPER = superior, lower = inferior).
 * Costs are null when N/A, {@code -1} when variable (X).
 */
public record CardBean(
        String id,
        String name,
        String displayName,
        boolean crypt,
        List<String> aka,
        List<String> sets,
        String cardText,
        String artist,
        boolean banned,
        boolean playtest,
        boolean unique,
        /** Raw "/"-joined type line, e.g. {@code "Action/Combat"} or {@code "Vampire"}. */
        String typeLine,
        /** Split type list: {@code ["Action","Combat"]} / {@code ["Vampire"]} / {@code ["Imbued"]}. */
        List<String> types,

        // ── crypt-only (null on a library card) ──
        String clan,
        String sect,
        String path,
        String group,
        Boolean advanced,
        Boolean infernal,
        Integer capacity,
        List<String> disciplines,
        String title,
        String votes,

        // ── library-only (null on a crypt card) ──
        String flavorText,
        List<String> requirementClans,
        String requirementPath,
        List<String> andDisciplines,
        List<String> orDisciplines,
        Integer poolCost,
        Integer bloodCost,
        Integer convictionCost,
        Boolean burnOption,
        String preamble,
        Boolean doNotReplace
) {

    public static CardBean of(Card card) {
        if (card instanceof CryptCard c) {
            return new CardBean(
                    c.id(), c.name(), c.displayName(), true,
                    c.aka(), c.sets(), c.cardText(), c.artist(), c.banned(), c.playtest(), c.unique(),
                    c.typeLine(), List.of(c.typeLine()),
                    c.clan(), c.sect(), c.path(), c.group(), c.advanced(), c.infernal(), c.capacity(),
                    c.disciplines(), c.title(), c.votes(),
                    null, null, null, null, null, null, null, null, null, null, null);
        }
        LibraryCard l = (LibraryCard) card;
        return new CardBean(
                l.id(), l.name(), l.displayName(), false,
                l.aka(), l.sets(), l.cardText(), l.artist(), l.banned(), l.playtest(), l.unique(),
                l.typeLine(), l.types(),
                null, null, null, null, null, null, null, null, null, null,
                l.flavorText(), l.requirementClans(), l.requirementPath(),
                l.andDisciplines(), l.orDisciplines(),
                l.poolCost(), l.bloodCost(), l.convictionCost(), l.burnOption(), l.preamble(), l.doNotReplace());
    }
}
