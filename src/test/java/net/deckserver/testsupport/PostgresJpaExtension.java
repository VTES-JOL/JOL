package net.deckserver.testsupport;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JUnit 5 extension backing the JPA test tier (net.deckserver.jpa.repository.*).
 *
 * <p>On first use in a JVM it starts a single PostgreSQL container via
 * Testcontainers and builds the {@code jol-test-pu} {@link EntityManagerFactory}
 * (see {@code src/test/resources/META-INF/persistence.xml}) against it.
 *
 * <p>Before <em>every</em> test class it runs {@code flyway clean} + {@code
 * migrate} against that container, so each class starts from an identical,
 * fully-migrated schema plus the declarative fixture in
 * {@code src/main/resources/db/testseed}. That mirrors the per-class schema
 * reset the old H2 {@code hbm2ddl=create-drop} setup gave for free, but now
 * against a real Postgres running the real migrations.
 *
 * <p>When Docker is not available the whole tier is skipped rather than failed
 * (see {@link #evaluateExecutionCondition}); CI runners have Docker, so the
 * tests still run there.
 *
 * <p>Usage: {@code @ExtendWith(PostgresJpaExtension.class)} on the test class,
 * then {@code emf = PostgresJpaExtension.emf()} in {@code @BeforeAll}.
 */
public class PostgresJpaExtension implements BeforeAllCallback, ExecutionCondition {

    private static final Logger logger = LoggerFactory.getLogger(PostgresJpaExtension.class);

    private static final String IMAGE = "postgres:16-alpine";
    /** Repository tests reset to a bare migrated schema (they insert their own rows). */
    private static final String[] MIGRATIONS_ONLY = {"classpath:db/migration"};
    /** FixtureDataTest additionally wants the declarative db/testseed fixture. */
    private static final String[] MIGRATIONS_AND_SEED = {"classpath:db/migration", "classpath:db/testseed"};

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static volatile PostgreSQLContainer<?> container;
    private static volatile EntityManagerFactory emf;
    private static volatile Boolean dockerAvailable;

    /** The shared, schema-reset-per-class EntityManagerFactory. */
    public static EntityManagerFactory emf() {
        if (emf == null) {
            throw new IllegalStateException(
                    "PostgresJpaExtension not initialised - is the test class annotated with "
                    + "@ExtendWith(PostgresJpaExtension.class)?");
        }
        return emf;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker available");
        }
        return ConditionEvaluationResult.disabled(
                "Docker is not available - skipping JPA test tier (Testcontainers PostgreSQL)");
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        ensureContainerAndEmf();
        resetSchema();
    }

    private static boolean isDockerAvailable() {
        if (dockerAvailable == null) {
            try {
                dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
            } catch (Throwable t) {
                dockerAvailable = false;
            }
        }
        return dockerAvailable;
    }

    private static synchronized void ensureContainerAndEmf() {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        container = new PostgreSQLContainer<>(IMAGE)
                .withDatabaseName("jol_test")
                .withUsername("jol")
                .withPassword("jol");
        container.start();
        logger.info("PostgresJpaExtension: started {} at {}", IMAGE, container.getJdbcUrl());

        // First migrate so the EMF's hbm2ddl=validate has a schema to check against.
        flyway(MIGRATIONS_ONLY).migrate();

        emf = Persistence.createEntityManagerFactory("jol-test-pu", Map.of(
                "jakarta.persistence.jdbc.url", container.getJdbcUrl(),
                "jakarta.persistence.jdbc.user", container.getUsername(),
                "jakarta.persistence.jdbc.password", container.getPassword()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (emf != null && emf.isOpen()) emf.close();
            } catch (Exception ignored) {
                // best effort
            }
            if (container != null) container.stop();
        }, "PostgresJpaExtension-shutdown"));
    }

    private static void resetSchema() {
        Flyway flyway = flyway(MIGRATIONS_ONLY);
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Applies the declarative fixture in {@code classpath:db/testseed} on top of
     * the already-migrated schema. Call from a {@code @BeforeAll} in a test that
     * reads the fixture; the next test class's schema reset drops it again.
     */
    public static void applyTestSeed() {
        flyway(MIGRATIONS_AND_SEED).migrate();
    }

    private static Flyway flyway(String[] locations) {
        return Flyway.configure()
                .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                .locations(locations)
                .cleanDisabled(false)
                .load();
    }
}
