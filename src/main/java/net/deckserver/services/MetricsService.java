package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import net.deckserver.game.model.GameNames;
import net.deckserver.jpa.entity.MetricEventEntity;
import net.deckserver.jpa.repository.MetricEventRepository;
import net.deckserver.jpa.repository.MetricEventRepository.Grain;
import net.deckserver.rest.bean.HeatmapCell;
import net.deckserver.rest.bean.MetricBucket;
import net.deckserver.rest.bean.MetricSeries;
import net.deckserver.rest.bean.MetricTotals;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Site-activity metrics — the successor to the {@code net.deckserver.metrics}
 * log4j2 CSV appender.
 *
 * <p>Writes are cheap and low-value-per-row, so {@link #record} only enqueues in
 * memory; {@link #persist()} bulk-inserts on the {@link PersistedService}
 * scheduler (1-minute interval) and on shutdown. Reads are ad-hoc aggregations
 * that go straight to JPA every call — there is no in-memory cache (same shape
 * as {@link HistoryService}).
 */
@Singleton
@Startup
public class MetricsService extends PersistedService {

    private static final MetricEventRepository repository = new MetricEventRepository();

    private static MetricsService instance() {
        return resolve(MetricsService.class, MetricsService::new);
    }

    private final Queue<MetricEventEntity> pending = new ConcurrentLinkedQueue<>();

    MetricsService() {
        super("MetricsService", 1); // batched flush, 1-minute interval
    }

    /** Record one game submit that carried a command and/or a chat message. */
    public static void record(String playerName, String gameName, boolean didCommand, boolean didChat) {
        if (playerName == null || playerName.isBlank() || gameName == null || gameName.isBlank()) return;
        if (!didCommand && !didChat) return;
        instance().pending.add(new MetricEventEntity(
                OffsetDateTime.now(), playerName, gameName, didCommand, didChat,
                GameNames.isTournament(gameName)));
    }

    public static List<MetricBucket> timeSeries(OffsetDateTime from, OffsetDateTime to, Grain grain,
                                                ZoneId zone, boolean tournamentOnly) {
        List<MetricBucket> r = instance().jpaRead(em -> repository.timeSeries(em, from, to, grain, zone, tournamentOnly));
        return r != null ? r : List.of();
    }

    public static List<MetricSeries> byPlayer(OffsetDateTime from, OffsetDateTime to, Grain grain,
                                              ZoneId zone, boolean tournamentOnly, int limit) {
        List<MetricSeries> r = instance().jpaRead(em -> repository.byDimension(em, false, from, to, grain, zone, tournamentOnly, limit));
        return r != null ? r : List.of();
    }

    public static List<MetricSeries> byGame(OffsetDateTime from, OffsetDateTime to, Grain grain,
                                            ZoneId zone, boolean tournamentOnly, int limit) {
        List<MetricSeries> r = instance().jpaRead(em -> repository.byDimension(em, true, from, to, grain, zone, tournamentOnly, limit));
        return r != null ? r : List.of();
    }

    public static List<HeatmapCell> heatmap(OffsetDateTime from, OffsetDateTime to, ZoneId zone, boolean tournamentOnly) {
        List<HeatmapCell> r = instance().jpaRead(em -> repository.heatmap(em, from, to, zone, tournamentOnly));
        return r != null ? r : List.of();
    }

    public static MetricTotals totals(OffsetDateTime from, OffsetDateTime to, ZoneId zone, boolean tournamentOnly) {
        MetricTotals r = instance().jpaRead(em -> repository.totals(em, from, to, zone, tournamentOnly));
        return r != null ? r : new MetricTotals(0, 0, 0, 0, 0, 0, null, null);
    }

    public static PersistedService getInstance() {
        return instance();
    }

    /** Test-only: number of events waiting for the next flush. */
    static int pendingCount() {
        return instance().pending.size();
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            return;
        }
        if (pending.isEmpty()) {
            return;
        }
        List<MetricEventEntity> batch = new ArrayList<>();
        MetricEventEntity event;
        while ((event = pending.poll()) != null) {
            batch.add(event);
        }
        logger.debug("Flushing {} metric events", batch.size());
        if (!jpaWrite(em -> repository.insertAll(em, batch))) {
            // Re-queue so the next flush retries rather than silently dropping.
            pending.addAll(batch);
        }
    }

    @Override
    protected void load() {
        // No startup load — reads go directly to JPA.
    }
}
