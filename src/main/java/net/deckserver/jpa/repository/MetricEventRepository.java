package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import net.deckserver.jpa.entity.MetricEventEntity;
import net.deckserver.rest.bean.HeatmapCell;
import net.deckserver.rest.bean.MetricBucket;
import net.deckserver.rest.bean.MetricSeries;
import net.deckserver.rest.bean.MetricTotals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Aggregation queries over {@code metric_event}. Reads go straight here on every
 * request (no in-memory cache) via {@link net.deckserver.services.MetricsService}.
 *
 * <p>The SQL is hand-written native and deliberately kept to the intersection of
 * PostgreSQL and H2 (PostgreSQL mode) so the repository tests can run on H2.
 * In particular:
 * <ul>
 *   <li>{@code x AT TIME ZONE 'zone'} yields a plain {@code timestamp} on PG but a
 *       still-zoned value on H2 — so the result is never CAST to {@code TIMESTAMP}
 *       (H2 would re-apply the session offset). Instead every zone-local field is
 *       pulled with {@code EXTRACT(... FROM (occurred_at AT TIME ZONE 'zone'))},
 *       which reads the same wall-clock component on both engines, and buckets are
 *       grouped by those integer components.</li>
 *   <li>{@code FILTER (WHERE ...)} and {@code EXTRACT(ISODOW ...)} are avoided
 *       (day-of-week is derived in Java).</li>
 * </ul>
 */
public class MetricEventRepository {

    /** Time-bucket granularity. */
    public enum Grain {
        HOUR(4), DAY(3), MONTH(2), YEAR(1);
        /** How many of year/month/day/hour components define a bucket at this grain. */
        private final int parts;
        Grain(int parts) { this.parts = parts; }
    }

    private static final DateTimeFormatter LOCAL_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String[] FIELDS = {"YEAR", "MONTH", "DAY", "HOUR"};

    // ── Writes ──────────────────────────────────────────────────────────────

    public void insertAll(EntityManager em, Collection<MetricEventEntity> events) {
        int n = 0;
        for (MetricEventEntity event : events) {
            em.persist(event);
            if (++n % 500 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    // ── Reads ───────────────────────────────────────────────────────────────

    public List<MetricBucket> timeSeries(EntityManager em, OffsetDateTime from, OffsetDateTime to,
                                         Grain grain, ZoneId zone, boolean tournamentOnly) {
        String comps = componentSelect(zone, grain.parts); // "EXTRACT(YEAR FROM (...)), EXTRACT(MONTH FROM (...))"
        @SuppressWarnings("unchecked")
        List<Object[]> rows = bind(em.createNativeQuery(
                "SELECT " + comps + ", " + AGG_COLS +
                        " FROM metric_event" +
                        " WHERE occurred_at >= :from AND occurred_at < :to" + tournamentClause(tournamentOnly) +
                        " GROUP BY " + comps + " ORDER BY " + comps), from, to).getResultList();

        List<MetricBucket> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(bucketRow(labelFromComponents(r, grain.parts), r, grain.parts));
        }
        return result;
    }

    public List<MetricSeries> byDimension(EntityManager em, boolean byGame, OffsetDateTime from, OffsetDateTime to,
                                          Grain grain, ZoneId zone, boolean tournamentOnly, int limit) {
        String dim = byGame ? "game_name" : "player_name";

        // Rank the dimension by total submits, then pull the per-bucket breakdown for the top N.
        @SuppressWarnings("unchecked")
        List<Object[]> topRows = bind(em.createNativeQuery(
                "SELECT " + dim + " AS k, COUNT(*) AS c" +
                        " FROM metric_event" +
                        " WHERE occurred_at >= :from AND occurred_at < :to" + tournamentClause(tournamentOnly) +
                        " GROUP BY " + dim + " ORDER BY c DESC, k ASC"),
                from, to).setMaxResults(Math.max(1, limit)).getResultList();
        if (topRows.isEmpty()) {
            return List.of();
        }
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Object[] r : topRows) {
            totals.put((String) r[0], asLong(r[1]));
        }

        int parts = grain.parts;
        String comps = componentSelect(zone, parts);
        String grouping = dim + ", " + comps;
        Query q = em.createNativeQuery(
                "SELECT " + dim + ", " + comps + ", " + AGG_COLS +
                        " FROM metric_event" +
                        " WHERE occurred_at >= :from AND occurred_at < :to" + tournamentClause(tournamentOnly) +
                        " AND " + dim + " IN (:keys)" +
                        " GROUP BY " + grouping + " ORDER BY " + grouping);
        bind(q, from, to).setParameter("keys", totals.keySet());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        Map<String, List<MetricBucket>> byKey = new LinkedHashMap<>();
        totals.keySet().forEach(k -> byKey.put(k, new ArrayList<>()));
        for (Object[] r : rows) {
            String key = (String) r[0];
            Object[] tail = new Object[r.length - 1];
            System.arraycopy(r, 1, tail, 0, tail.length);
            byKey.get(key).add(bucketRow(labelFromComponents(tail, parts), tail, parts));
        }
        List<MetricSeries> result = new ArrayList<>(totals.size());
        totals.forEach((key, total) -> result.add(new MetricSeries(key, total, byKey.get(key))));
        return result;
    }

    public List<HeatmapCell> heatmap(EntityManager em, OffsetDateTime from, OffsetDateTime to,
                                     ZoneId zone, boolean tournamentOnly) {
        String comps = componentSelect(zone, 4); // year, month, day, hour
        @SuppressWarnings("unchecked")
        List<Object[]> rows = bind(em.createNativeQuery(
                "SELECT " + comps + ", COUNT(*) AS c" +
                        " FROM metric_event" +
                        " WHERE occurred_at >= :from AND occurred_at < :to" + tournamentClause(tournamentOnly) +
                        " GROUP BY " + comps), from, to).getResultList();

        long[][] grid = new long[8][24]; // [ISO day-of-week 1..7][hour 0..23]
        for (Object[] r : rows) {
            int year = (int) asLong(r[0]);
            int month = (int) asLong(r[1]);
            int day = (int) asLong(r[2]);
            int hour = (int) asLong(r[3]);
            int dow = LocalDate.of(year, month, day).getDayOfWeek().getValue();
            grid[dow][hour] += asLong(r[4]);
        }
        List<HeatmapCell> result = new ArrayList<>();
        for (int dow = 1; dow <= 7; dow++) {
            for (int hour = 0; hour < 24; hour++) {
                if (grid[dow][hour] > 0) {
                    result.add(new HeatmapCell(dow, hour, grid[dow][hour]));
                }
            }
        }
        return result;
    }

    public MetricTotals totals(EntityManager em, OffsetDateTime from, OffsetDateTime to,
                               ZoneId zone, boolean tournamentOnly) {
        String z = zoneExpr(zone);
        // Synthetic yyyymmdd integer for a portable COUNT(DISTINCT <day>).
        String dayKey = "(EXTRACT(YEAR FROM " + z + ") * 10000 + EXTRACT(MONTH FROM " + z + ") * 100 + EXTRACT(DAY FROM " + z + "))";
        Object[] r = (Object[]) bind(em.createNativeQuery(
                "SELECT " + AGG_COLS + ", COUNT(DISTINCT " + dayKey + ") AS days," +
                        " MIN(occurred_at) AS first_at, MAX(occurred_at) AS last_at" +
                        " FROM metric_event" +
                        " WHERE occurred_at >= :from AND occurred_at < :to" + tournamentClause(tournamentOnly)),
                from, to).getSingleResult();
        // AGG_COLS occupies indexes 0..4.
        return new MetricTotals(
                asLong(r[0]), asLong(r[1]), asLong(r[2]), asLong(r[3]), asLong(r[4]),
                asLong(r[5]),
                asOffsetUtc(r[6]),
                asOffsetUtc(r[7]));
    }

    // ── SQL fragment builders ───────────────────────────────────────────────

    private static final String AGG_COLS =
            "COUNT(*) AS submits," +
            " SUM(CASE WHEN did_command THEN 1 ELSE 0 END) AS commands," +
            " SUM(CASE WHEN did_chat THEN 1 ELSE 0 END) AS chats," +
            " COUNT(DISTINCT player_name) AS active_players," +
            " COUNT(DISTINCT game_name) AS active_games";

    /**
     * {@code occurred_at} shifted to {@code zone}. On PG this is a plain
     * {@code timestamp} (wall clock in zone); on H2 it stays zoned but with
     * {@code zone}'s offset — either way {@code EXTRACT} reads the same fields.
     * The id is spliced as a literal (both engines take a string here); it comes
     * from {@link ZoneId} so it can only contain {@code [A-Za-z0-9/_+:-]}, but
     * guard anyway.
     */
    private static String zoneExpr(ZoneId zone) {
        String id = zone.getId();
        if (!id.matches("[A-Za-z0-9/_+:-]+")) {
            throw new IllegalArgumentException("Unsupported zone id: " + id);
        }
        return "(occurred_at AT TIME ZONE '" + id + "')";
    }

    /** {@code "EXTRACT(YEAR FROM z), EXTRACT(MONTH FROM z), ..."} for the first {@code parts} fields. */
    private static String componentSelect(ZoneId zone, int parts) {
        String z = zoneExpr(zone);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts; i++) {
            if (i > 0) sb.append(", ");
            sb.append("EXTRACT(").append(FIELDS[i]).append(" FROM ").append(z).append(')');
        }
        return sb.toString();
    }

    private static String tournamentClause(boolean tournamentOnly) {
        return tournamentOnly ? " AND is_tournament = TRUE" : "";
    }

    private static Query bind(Query q, OffsetDateTime from, OffsetDateTime to) {
        return q.setParameter("from", from).setParameter("to", to);
    }

    // ── Row mapping ─────────────────────────────────────────────────────────

    private static String labelFromComponents(Object[] row, int parts) {
        int year = (int) asLong(row[0]);
        int month = parts >= 2 ? (int) asLong(row[1]) : 1;
        int day = parts >= 3 ? (int) asLong(row[2]) : 1;
        int hour = parts >= 4 ? (int) asLong(row[3]) : 0;
        return LocalDateTime.of(year, month, day, hour, 0).format(LOCAL_ISO);
    }

    private static MetricBucket bucketRow(String label, Object[] row, int offset) {
        return new MetricBucket(label,
                asLong(row[offset]), asLong(row[offset + 1]), asLong(row[offset + 2]),
                asLong(row[offset + 3]), asLong(row[offset + 4]));
    }

    private static long asLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        if (o instanceof BigInteger b) return b.longValue();
        if (o instanceof BigDecimal b) return b.longValue();
        return Long.parseLong(o.toString().trim());
    }

    private static String asOffsetUtc(Object o) {
        if (o == null) return null;
        OffsetDateTime odt;
        if (o instanceof OffsetDateTime v) {
            odt = v;
        } else if (o instanceof java.sql.Timestamp ts) {
            odt = ts.toInstant().atOffset(ZoneOffset.UTC);
        } else if (o instanceof java.time.Instant i) {
            odt = i.atOffset(ZoneOffset.UTC);
        } else if (o instanceof LocalDateTime ldt) {
            odt = ldt.atOffset(ZoneOffset.UTC);
        } else {
            return o.toString();
        }
        return odt.withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
