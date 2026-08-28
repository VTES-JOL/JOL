package net.deckserver.rest.bean;

import java.util.List;

/**
 * Full card data for the deck editor — one shape covers autocomplete
 * suggestions, deck-entry enrichment and icon rendering. Projected from
 * {@link net.deckserver.game.cards.Card} by {@code CardSearchService}.
 *
 * <p>Crypt cards: {@code types} = {@code ["Vampire"]} / {@code ["Imbued"]},
 * {@code group} = {@code "1"}–{@code "7"} | {@code "ANY"}. Library cards:
 * {@code types} = the card's type list, {@code group} = null.
 */
public record CardDetailBean(
        String id,
        String name,
        boolean crypt,
        // entry metadata
        List<String> types,
        String group,
        boolean banned,
        boolean advanced,
        List<String> sets,
        // crypt display
        String clan,
        String path,
        Integer capacity,
        List<String> disciplines,
        // library display
        List<String> andDisciplines,
        List<String> orDisciplines,
        List<String> requirementClans,
        String requirementPath,
        Integer poolCost,
        Integer bloodCost
) {
}
