package net.deckserver.game.cards;

import java.util.List;

/**
 * A library card.
 *
 * <p>{@code types} — one or more card types, split from the "/"-delimited Type
 * column (e.g. {@code ["Action", "Combat"]}).
 *
 * <p>{@code requirementClans} — zero or more clan requirements, split from the
 * "/"-delimited Clan column.
 *
 * <p>{@code andDisciplines} — disciplines where ALL must be present on the
 * acting minion (the "A &amp; B" form in the data). {@code orDisciplines} —
 * disciplines where ANY ONE is sufficient (the "A/B/C" form); a lone discipline
 * with no delimiter is placed here too.
 *
 * <p>Costs are null when not applicable, {@code -1} when variable (X).
 * {@code burnOption} is true when the Burn Option column is "Y"/"Yes".
 */
public record LibraryCard(
        String id,
        String name,
        List<String> aka,
        List<String> sets,
        String cardText,
        String artist,
        boolean banned,
        String flavorText,
        List<String> types,
        List<String> requirementClans,
        String requirementPath,
        List<String> andDisciplines,
        List<String> orDisciplines,
        Integer poolCost,
        Integer bloodCost,
        Integer convictionCost,
        boolean burnOption
) implements Card {
}
