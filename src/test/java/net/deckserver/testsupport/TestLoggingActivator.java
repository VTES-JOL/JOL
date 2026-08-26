package net.deckserver.testsupport;

import io.quarkus.bootstrap.logging.InitialConfigurator;
import org.jboss.logmanager.formatters.PatternFormatter;
import org.jboss.logmanager.handlers.ConsoleHandler;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Registered via META-INF/services/org.junit.platform.launcher.TestExecutionListener
 * so it runs for every Surefire test run without any test class needing to
 * opt in. Fixes test-scope SLF4J logging (e.g. CardDatabaseBuilder's,
 * JolFixtureLoader's) being silently dropped.
 *
 * The actual cause: org.jboss.slf4j:slf4j-jboss-logmanager wins the
 * SLF4J-provider race on this classpath (same as in the running app — see
 * log4j2.xml's Root/Loggers comment and application.properties'
 * quarkus.log.console.format), but quarkus-bootstrap-runner's own
 * InitialConfigurator installs InitialConfigurator.DELAYED_HANDLER as
 * every logger's initial handler — a buffer that queues records until
 * Quarkus's real runtime bootstrap "activates" it with real handlers.
 * That activation only happens during an actual Quarkus application
 * startup, which these tests deliberately skip (JolServiceExtension boots
 * a bare H2 EntityManagerFactory instead — see its own class comment), so
 * the buffer just sits there, unactivated, silently discarding everything
 * ever written through it. Activating it directly with a plain console
 * handler here is the same thing Quarkus's own runtime does once its
 * logging config is known, just done immediately instead.
 */
public class TestLoggingActivator implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if (InitialConfigurator.DELAYED_HANDLER.isActivated()) {
            return;
        }
        ConsoleHandler console = new ConsoleHandler(new PatternFormatter("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %c{1} - %m%n"));
        console.setLevel(Level.INFO);
        // setHandlers (not setBuildTimeHandlers/buildTimeComplete, which is
        // a transient build-time-only flush that leaves the handler
        // un-activated again afterward — see QuarkusDelayedHandler's
        // source) is what actually flips its internal `activated` flag
        // permanently, so every log call after this point delegates
        // straight through instead of being queued/dropped.
        InitialConfigurator.DELAYED_HANDLER.setHandlers(new Handler[] {console});
    }
}
