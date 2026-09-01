package net.deckserver.rest.bean;

/**
 * Single-row KPI summary for a window. {@code firstEvent}/{@code lastEvent} are
 * ISO offset timestamps (UTC) of the earliest/latest metric row in range, or
 * {@code null} when the window is empty.
 */
public record MetricTotals(
        long submits,
        long commands,
        long chats,
        long activePlayers,
        long activeGames,
        long activeDays,
        String firstEvent,
        String lastEvent
) {
}
