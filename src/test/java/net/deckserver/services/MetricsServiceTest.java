package net.deckserver.services;

import net.deckserver.jpa.repository.MetricEventRepository.Grain;
import net.deckserver.rest.bean.MetricTotals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class MetricsServiceTest {

    private static final OffsetDateTime FROM = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TO = OffsetDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void recordEnqueuesActivity() {
        int before = MetricsService.pendingCount();
        MetricsService.record("Player1", "Test Game", true, false);
        MetricsService.record("Player2", "Test Game", false, true);
        assertThat(MetricsService.pendingCount(), is(before + 2));
    }

    @Test
    void recordIgnoresBlankAndNoActivityRows() {
        int before = MetricsService.pendingCount();
        MetricsService.record(null, "Test Game", true, false);
        MetricsService.record("Player1", " ", true, false);
        MetricsService.record("Player1", "Test Game", false, false);
        assertThat(MetricsService.pendingCount(), is(before));
    }

    @Test
    void readsReturnEmptyRatherThanNullWhenNoData() {
        assertThat(MetricsService.timeSeries(FROM, TO, Grain.DAY, ZoneOffset.UTC, false), is(empty()));
        assertThat(MetricsService.byPlayer(FROM, TO, Grain.DAY, ZoneOffset.UTC, false, 10), is(empty()));
        assertThat(MetricsService.byGame(FROM, TO, Grain.DAY, ZoneOffset.UTC, false, 10), is(empty()));
        assertThat(MetricsService.heatmap(FROM, TO, ZoneOffset.UTC, false), is(empty()));

        MetricTotals totals = MetricsService.totals(FROM, TO, ZoneOffset.UTC, false);
        assertThat(totals.submits(), is(0L));
        assertThat(totals.firstEvent(), nullValue());
    }
}
