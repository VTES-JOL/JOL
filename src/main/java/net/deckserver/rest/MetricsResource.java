package net.deckserver.rest;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import net.deckserver.jpa.repository.MetricEventRepository.Grain;
import net.deckserver.rest.bean.HeatmapCell;
import net.deckserver.rest.bean.MetricBucket;
import net.deckserver.rest.bean.MetricSeries;
import net.deckserver.rest.bean.MetricTotals;
import net.deckserver.services.MetricsService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Public read-only analytics over {@code metric_event} (site activity — game
 * submits carrying a command and/or chat). Backs the metrics dashboards; no
 * auth (added to {@code quarkus.http.auth.permission.public.paths}).
 *
 * <p>Window: {@code from}/{@code to} are calendar dates ({@code YYYY-MM-DD})
 * interpreted in UTC, {@code to} inclusive. Omitted ⇒ the trailing 90 days;
 * {@code all=true} ⇒ everything. The {@code tz} param (IANA id, default UTC)
 * only shifts how rows are bucketed/labelled, not the window edges.
 */
@Path("metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource extends BaseResource {

    private static final int DEFAULT_WINDOW_DAYS = 90;
    private static final int MAX_LIMIT = 200;

    @GET
    @Path("timeseries")
    public List<MetricBucket> timeseries(@QueryParam("from") String from,
                                         @QueryParam("to") String to,
                                         @QueryParam("all") @DefaultValue("false") boolean all,
                                         @QueryParam("grain") @DefaultValue("day") String grain,
                                         @QueryParam("tz") @DefaultValue("UTC") String tz,
                                         @QueryParam("tournamentOnly") @DefaultValue("false") boolean tournamentOnly) {
        Window w = window(from, to, all);
        return MetricsService.timeSeries(w.from(), w.to(), grain(grain), zone(tz), tournamentOnly);
    }

    @GET
    @Path("by-player")
    public List<MetricSeries> byPlayer(@QueryParam("from") String from,
                                       @QueryParam("to") String to,
                                       @QueryParam("all") @DefaultValue("false") boolean all,
                                       @QueryParam("grain") @DefaultValue("day") String grain,
                                       @QueryParam("tz") @DefaultValue("UTC") String tz,
                                       @QueryParam("tournamentOnly") @DefaultValue("false") boolean tournamentOnly,
                                       @QueryParam("limit") @DefaultValue("25") int limit) {
        Window w = window(from, to, all);
        return MetricsService.byPlayer(w.from(), w.to(), grain(grain), zone(tz), tournamentOnly, clampLimit(limit));
    }

    @GET
    @Path("by-game")
    public List<MetricSeries> byGame(@QueryParam("from") String from,
                                     @QueryParam("to") String to,
                                     @QueryParam("all") @DefaultValue("false") boolean all,
                                     @QueryParam("grain") @DefaultValue("day") String grain,
                                     @QueryParam("tz") @DefaultValue("UTC") String tz,
                                     @QueryParam("tournamentOnly") @DefaultValue("false") boolean tournamentOnly,
                                     @QueryParam("limit") @DefaultValue("25") int limit) {
        Window w = window(from, to, all);
        return MetricsService.byGame(w.from(), w.to(), grain(grain), zone(tz), tournamentOnly, clampLimit(limit));
    }

    @GET
    @Path("heatmap")
    public List<HeatmapCell> heatmap(@QueryParam("from") String from,
                                     @QueryParam("to") String to,
                                     @QueryParam("all") @DefaultValue("false") boolean all,
                                     @QueryParam("tz") @DefaultValue("UTC") String tz,
                                     @QueryParam("tournamentOnly") @DefaultValue("false") boolean tournamentOnly) {
        Window w = window(from, to, all);
        return MetricsService.heatmap(w.from(), w.to(), zone(tz), tournamentOnly);
    }

    @GET
    @Path("totals")
    public MetricTotals totals(@QueryParam("from") String from,
                               @QueryParam("to") String to,
                               @QueryParam("all") @DefaultValue("false") boolean all,
                               @QueryParam("tz") @DefaultValue("UTC") String tz,
                               @QueryParam("tournamentOnly") @DefaultValue("false") boolean tournamentOnly) {
        Window w = window(from, to, all);
        return MetricsService.totals(w.from(), w.to(), zone(tz), tournamentOnly);
    }

    // ── param parsing ───────────────────────────────────────────────────────

    private record Window(OffsetDateTime from, OffsetDateTime to) {
    }

    private static Window window(String from, String to, boolean all) {
        if (all) {
            return new Window(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                    LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
        }
        LocalDate toDate = to == null || to.isBlank() ? LocalDate.now(ZoneOffset.UTC) : parseDate(to, "to");
        LocalDate fromDate = from == null || from.isBlank() ? toDate.minusDays(DEFAULT_WINDOW_DAYS) : parseDate(from, "from");
        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("'from' (" + fromDate + ") is after 'to' (" + toDate + ")");
        }
        // 'to' inclusive → exclusive upper bound is the start of the next day.
        return new Window(fromDate.atStartOfDay().atOffset(ZoneOffset.UTC),
                toDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    private static LocalDate parseDate(String value, String field) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("'" + field + "' is not an ISO date (YYYY-MM-DD): " + value);
        }
    }

    private static Grain grain(String value) {
        try {
            return Grain.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("'grain' must be one of hour, day, month, year — got: " + value);
        }
    }

    private static ZoneId zone(String value) {
        try {
            return ZoneId.of(value.trim());
        } catch (DateTimeException e) {
            throw new BadRequestException("'tz' is not a valid IANA timezone id: " + value);
        }
    }

    private static int clampLimit(int limit) {
        if (limit < 1) return 1;
        return Math.min(limit, MAX_LIMIT);
    }
}
