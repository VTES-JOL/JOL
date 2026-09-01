package net.deckserver.rest.bean;

/**
 * One time bucket of activity metrics. {@code bucket} is a local-time ISO
 * timestamp (no offset) at the start of the interval, in the reporting
 * timezone the query was run with — e.g. {@code "2026-08-26T00:00:00"} for a
 * daily bucket. {@code submits} counts metric rows; a row can be both a
 * command and a chat, so {@code commands + chats >= submits}.
 */
public record MetricBucket(
        String bucket,
        long submits,
        long commands,
        long chats,
        long activePlayers,
        long activeGames
) {
}
