package net.deckserver.jobs;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import net.deckserver.services.PersistedService;
import net.deckserver.services.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quarkus replacement for the scheduled-job wiring that used to live in
 * {@code JolApplicationInitializer} — a {@code javax} {@code ServletContextListener}
 * that is never invoked under Quarkus's jar/Vert.x runtime, which left every job
 * in {@link net.deckserver.jobs} unscheduled after the migration.
 * <p>
 * One {@code @Scheduled} method per periodic job, plus the one-shot
 * {@link RegistrationReconciliation} as a late {@link StartupEvent} observer
 * (priority well above the {@code @Startup} services at 2500, so their in-memory
 * state is loaded before it runs — same ordering trick as
 * {@code net.deckserver.jpa.JpaStartup}, at the other end).
 * <p>
 * Cadence is carried over verbatim from the old
 * {@code scheduler.scheduleAtFixedRate(...)} calls:
 * <pre>
 *   PublicGameBuilder                   - every 1m, first run after 1m
 *   GameCleanUp                         - every 1m, first run after 1m
 *   TournamentJob                       - every 1m, first run immediately
 *   RefreshTokenService.cleanupExpired  - every 24h, first run after 5m
 *   RegistrationReconciliation          - once, at startup
 * </pre>
 * Every body early-returns when {@code ENABLE_TEST_MODE} is set; the scheduler
 * is also disabled outright under the {@code %test} profile
 * (see {@code application.properties}), so {@code @QuarkusTest} runs never touch
 * these. {@link Scheduled.ConcurrentExecution#SKIP} keeps a slow run from
 * overlapping its next tick (the old single-thread executor serialised all jobs
 * onto one thread; per-method SKIP is the nearest equivalent).
 */
@ApplicationScoped
public class ScheduledJobs {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledJobs.class);

    private final PublicGameBuilder publicGameBuilder = new PublicGameBuilder();
    private final GameCleanUp gameCleanUp = new GameCleanUp();
    private final TournamentJob tournamentJob = new TournamentJob();

    @Scheduled(every = "1m", delayed = "1m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void buildPublicGames() {
        if (PersistedService.isTestMode()) return;
        publicGameBuilder.run();
    }

    @Scheduled(every = "1m", delayed = "1m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cleanUpGames() {
        if (PersistedService.isTestMode()) return;
        gameCleanUp.run();
    }

    @Scheduled(every = "1m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void runTournamentJob() {
        if (PersistedService.isTestMode()) return;
        tournamentJob.run();
    }

    @Scheduled(every = "24h", delayed = "5m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cleanUpExpiredRefreshTokens() {
        if (PersistedService.isTestMode()) return;
        RefreshTokenService.cleanupExpired();
    }

    void reconcileRegistrationsAtStartup(@Observes @Priority(10_000) StartupEvent ev) {
        if (PersistedService.isTestMode()) return;
        logger.info("Running registration reconciliation at startup");
        new RegistrationReconciliation().run();
    }
}
