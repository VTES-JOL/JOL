package net.deckserver.rest.bean;

/**
 * One cell of the day-of-week × hour-of-day activity grid, in the reporting
 * timezone. {@code dayOfWeek} is ISO-8601 (1 = Monday … 7 = Sunday);
 * {@code hourOfDay} is 0–23. Cells with no activity are omitted.
 */
public record HeatmapCell(
        int dayOfWeek,
        int hourOfDay,
        long submits
) {
}
