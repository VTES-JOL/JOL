package net.deckserver.rest;

import jakarta.ws.rs.BadRequestException;
import net.deckserver.services.JolServiceExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises MetricsResource's query-param parsing / validation directly (the
 * repo has no @QuarkusTest harness). The service read path runs against the H2
 * fixture DB, which has no metric_event rows — so valid calls return empty.
 */
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class MetricsResourceTest {

    private final MetricsResource resource = new MetricsResource();

    @Test
    void validDefaultedCallReturnsEmpty() {
        assertThat(resource.timeseries(null, null, false, "day", "UTC", false), is(empty()));
        assertThat(resource.totals(null, null, false, "UTC", false).submits(), is(0L));
    }

    @Test
    void allFlagWidensWindowWithoutError() {
        assertThat(resource.timeseries(null, null, true, "month", "UTC", false), is(empty()));
    }

    @Test
    void rejectsBadGrain() {
        assertThrows(BadRequestException.class,
                () -> resource.timeseries("2026-01-01", "2026-02-01", false, "fortnight", "UTC", false));
    }

    @Test
    void rejectsBadTimezone() {
        assertThrows(BadRequestException.class,
                () -> resource.heatmap("2026-01-01", "2026-02-01", false, "Mars/Olympus", false));
    }

    @Test
    void rejectsBadDate() {
        assertThrows(BadRequestException.class,
                () -> resource.timeseries("01-01-2026", null, false, "day", "UTC", false));
    }

    @Test
    void rejectsFromAfterTo() {
        assertThrows(BadRequestException.class,
                () -> resource.timeseries("2026-06-01", "2026-05-01", false, "day", "UTC", false));
    }

    @Test
    void grainAndTimezoneAreCaseAndWhitespaceTolerant() {
        assertThat(resource.timeseries(null, null, false, " Day ", " UTC ", false), is(empty()));
    }
}
