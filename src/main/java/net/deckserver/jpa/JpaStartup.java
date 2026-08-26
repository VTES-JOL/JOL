package net.deckserver.jpa;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import net.deckserver.services.PersistedService;

/**
 * Replaces the one thing JolApplicationInitializer (a javax ServletContextListener,
 * never invoked under Quarkus's jar/Vert.x runtime) used to guarantee: that
 * JpaFactory.initialize() runs before any service singleton is first touched.
 * <p>
 * Every PersistedService subclass is now an {@code @Startup @ApplicationScoped}
 * CDI bean (see PersistedService/e.g. GameService) — Quarkus's {@code @Startup}
 * fires at CDI observer priority 2500 by default. Without an explicit lower
 * priority here, this observer's relative order against those beans' eager
 * creation is undefined — confirmed empirically running quarkus:dev: some
 * services loaded successfully, others threw "JPA not initialized" because
 * they were constructed before this ran. @Priority(1) guarantees this always
 * runs first.
 * <p>
 * JolApplicationInitializer itself is left in place for now — it still holds
 * scheduled-job/WebSocket-registration/service-shutdown logic that Phase 2/3
 * will migrate, at which point that whole class (and this one, folded into
 * its replacement) gets retired.
 */
@ApplicationScoped
public class JpaStartup {

    void onStart(@Observes @Priority(1) StartupEvent ev) {
        if (!PersistedService.isTestMode()) {
            JpaFactory.initialize();
        }
    }
}
