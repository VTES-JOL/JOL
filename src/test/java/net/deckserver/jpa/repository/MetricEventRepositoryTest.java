package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import net.deckserver.jpa.entity.MetricEventEntity;
import net.deckserver.jpa.repository.MetricEventRepository.Grain;
import net.deckserver.rest.bean.HeatmapCell;
import net.deckserver.rest.bean.MetricBucket;
import net.deckserver.rest.bean.MetricSeries;
import net.deckserver.rest.bean.MetricTotals;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(PostgresJpaExtension.class)
class MetricEventRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    final MetricEventRepository repo = new MetricEventRepository();

    static final ZoneId UTC = ZoneOffset.UTC;
    static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
    }

    @AfterAll
    static void tearDownEmf() {
        /* shared EMF: closed by PostgresJpaExtension, not per-class */;
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        em.close();
    }

    private void event(String isoInstant, String player, String game, boolean cmd, boolean chat, boolean tourn) {
        em.persist(new MetricEventEntity(OffsetDateTime.parse(isoInstant), player, game, cmd, chat, tourn));
    }

    private OffsetDateTime day(String d) {
        return OffsetDateTime.parse(d + "T00:00:00Z");
    }

    @Test
    void dailyTimeSeriesBucketsAndCounts() {
        event("2026-06-01T10:00:00Z", "P1", "G1", true, false, false);
        event("2026-06-01T11:00:00Z", "P2", "G1", false, true, false);
        event("2026-06-01T12:00:00Z", "P1", "G2", true, true, false);
        event("2026-06-03T09:00:00Z", "P3", "G3", true, false, false);
        em.flush();

        List<MetricBucket> series = repo.timeSeries(em, day("2026-06-01"), day("2026-06-04"),
                Grain.DAY, UTC, false);

        assertThat(series, hasSize(2));
        MetricBucket d1 = series.get(0);
        assertThat(d1.bucket(), is("2026-06-01T00:00:00"));
        assertThat(d1.submits(), is(3L));
        assertThat(d1.commands(), is(2L));
        assertThat(d1.chats(), is(2L));
        assertThat(d1.activePlayers(), is(2L));
        assertThat(d1.activeGames(), is(2L));

        MetricBucket d3 = series.get(1);
        assertThat(d3.bucket(), is("2026-06-03T00:00:00"));
        assertThat(d3.submits(), is(1L));
    }

    @Test
    void windowUpperBoundIsExclusive() {
        event("2026-06-01T00:00:00Z", "P1", "G1", true, false, false);
        event("2026-06-04T00:00:00Z", "P1", "G1", true, false, false);
        em.flush();

        List<MetricBucket> series = repo.timeSeries(em, day("2026-06-01"), day("2026-06-04"),
                Grain.DAY, UTC, false);
        assertThat(series, hasSize(1));
        assertThat(series.get(0).bucket(), is("2026-06-01T00:00:00"));
    }

    @Test
    void tournamentOnlyFilters() {
        event("2026-06-01T10:00:00Z", "P1", "Casual", true, false, false);
        event("2026-06-01T11:00:00Z", "P2", "Round 1 - Table 1", true, false, true);
        em.flush();

        List<MetricBucket> all = repo.timeSeries(em, day("2026-06-01"), day("2026-06-02"), Grain.DAY, UTC, false);
        List<MetricBucket> tourn = repo.timeSeries(em, day("2026-06-01"), day("2026-06-02"), Grain.DAY, UTC, true);

        assertThat(all.get(0).submits(), is(2L));
        assertThat(tourn.get(0).submits(), is(1L));
    }

    @Test
    void timezoneShiftsBucketBoundary() {
        // 23:30 UTC on Jun 1 is 09:30 on Jun 2 in Sydney (UTC+10, no DST in June).
        event("2026-06-01T23:30:00Z", "P1", "G1", true, false, false);
        em.flush();

        List<MetricBucket> utc = repo.timeSeries(em, day("2026-06-01"), day("2026-06-03"), Grain.DAY, UTC, false);
        List<MetricBucket> syd = repo.timeSeries(em, day("2026-06-01"), day("2026-06-03"), Grain.DAY, SYDNEY, false);

        assertThat(utc.get(0).bucket(), is("2026-06-01T00:00:00"));
        assertThat(syd.get(0).bucket(), is("2026-06-02T00:00:00"));
    }

    @Test
    void monthlyAndYearlyGrains() {
        event("2026-01-15T10:00:00Z", "P1", "G1", true, false, false);
        event("2026-02-20T10:00:00Z", "P1", "G1", true, false, false);
        event("2025-11-01T10:00:00Z", "P1", "G1", true, false, false);
        em.flush();

        List<MetricBucket> months = repo.timeSeries(em, day("2025-01-01"), day("2026-12-31"), Grain.MONTH, UTC, false);
        assertThat(months.stream().map(MetricBucket::bucket).toList(),
                contains("2025-11-01T00:00:00", "2026-01-01T00:00:00", "2026-02-01T00:00:00"));

        List<MetricBucket> years = repo.timeSeries(em, day("2025-01-01"), day("2026-12-31"), Grain.YEAR, UTC, false);
        assertThat(years.stream().map(MetricBucket::bucket).toList(),
                contains("2025-01-01T00:00:00", "2026-01-01T00:00:00"));
        assertThat(years.get(1).submits(), is(2L));
    }

    @Test
    void byPlayerRanksAndLimits() {
        for (int i = 0; i < 3; i++) event("2026-06-0" + (i + 1) + "T10:00:00Z", "Heavy", "G1", true, false, false);
        event("2026-06-01T10:00:00Z", "Light", "G1", false, true, false);
        em.flush();

        List<MetricSeries> top = repo.byDimension(em, false, day("2026-06-01"), day("2026-06-10"),
                Grain.DAY, UTC, false, 1);
        assertThat(top, hasSize(1));
        assertThat(top.get(0).key(), is("Heavy"));
        assertThat(top.get(0).submits(), is(3L));
        assertThat(top.get(0).buckets(), hasSize(3));

        List<MetricSeries> both = repo.byDimension(em, false, day("2026-06-01"), day("2026-06-10"),
                Grain.DAY, UTC, false, 10);
        assertThat(both.stream().map(MetricSeries::key).toList(), contains("Heavy", "Light"));
    }

    @Test
    void heatmapUsesIsoDayOfWeekAndLocalHour() {
        // 2026-06-01 is a Monday. 08:00 UTC → hour 8, ISO dow 1.
        event("2026-06-01T08:00:00Z", "P1", "G1", true, false, false);
        event("2026-06-01T08:15:00Z", "P2", "G1", true, false, false);
        em.flush();

        List<HeatmapCell> cells = repo.heatmap(em, day("2026-06-01"), day("2026-06-02"), UTC, false);
        assertThat(cells, hasSize(1));
        assertThat(cells.get(0).dayOfWeek(), is(1));
        assertThat(cells.get(0).hourOfDay(), is(8));
        assertThat(cells.get(0).submits(), is(2L));
    }

    @Test
    void totalsSummariseWindow() {
        event("2026-06-01T10:00:00Z", "P1", "G1", true, false, false);
        event("2026-06-01T12:00:00Z", "P2", "G1", false, true, false);
        event("2026-06-05T12:00:00Z", "P1", "G2", true, true, false);
        em.flush();

        MetricTotals t = repo.totals(em, day("2026-06-01"), day("2026-06-10"), UTC, false);
        assertThat(t.submits(), is(3L));
        assertThat(t.commands(), is(2L));
        assertThat(t.chats(), is(2L));
        assertThat(t.activePlayers(), is(2L));
        assertThat(t.activeGames(), is(2L));
        assertThat(t.activeDays(), is(2L));
        assertThat(t.firstEvent(), startsWith("2026-06-01T10:00"));
        assertThat(t.lastEvent(), startsWith("2026-06-05T12:00"));
    }

    @Test
    void totalsOnEmptyWindow() {
        MetricTotals t = repo.totals(em, day("2020-01-01"), day("2020-01-02"), UTC, false);
        assertThat(t.submits(), is(0L));
        assertThat(t.firstEvent(), nullValue());
        assertThat(t.lastEvent(), nullValue());
    }
}
