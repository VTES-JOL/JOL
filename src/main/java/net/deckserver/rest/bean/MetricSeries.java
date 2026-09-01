package net.deckserver.rest.bean;

import java.util.List;

/**
 * A named time series — {@code key} is a player name (/metrics/by-player) or a
 * game name (/metrics/by-game). Series are returned most-active first.
 */
public record MetricSeries(
        String key,
        long submits,
        List<MetricBucket> buckets
) {
}
