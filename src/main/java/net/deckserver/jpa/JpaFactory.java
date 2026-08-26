package net.deckserver.jpa;

import io.quarkus.arc.Arc;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin facade over whichever EntityManagerFactory is active — kept as a
 * static facade (rather than switching every call site to CDI injection
 * directly) so the 21 service singletons and the test harness
 * (JolServiceExtension/JolFixtureLoader) need zero changes for this phase of
 * the Quarkus migration; converting those services to CDI beans that inject
 * EntityManager directly is Phase 2's job, not this one.
 * <p>
 * Production path ({@link #initialize()}): the EntityManagerFactory is built
 * and owned by Quarkus's own quarkus-hibernate-orm extension (configured via
 * application.properties) — this just looks it up via Arc (Quarkus's CDI
 * container), it doesn't create or own it. Schema migration is handled
 * automatically by the quarkus-flyway extension at Quarkus startup, not by
 * this class.
 * <p>
 * Test path ({@link #initializeWithEmf}): unchanged — JolServiceExtension
 * builds its own H2-backed EntityManagerFactory directly against
 * src/test/resources/META-INF/persistence.xml, entirely independent of
 * Quarkus's datasource/EMF.
 */
public final class JpaFactory {

    private static final Logger logger = LoggerFactory.getLogger(JpaFactory.class);
    private static volatile EntityManagerFactory emf;
    // Only the test path hands us an EMF we're responsible for closing — the
    // production one is a container-managed bean Quarkus closes itself on
    // its own shutdown.
    private static volatile boolean ownsEmf;

    private JpaFactory() {}

    public static void initialize() {
        emf = Arc.container().instance(EntityManagerFactory.class).get();
        ownsEmf = false;
        logger.info("JPA initialized via Quarkus-managed EntityManagerFactory");
    }

    public static void initializeWithEmf(EntityManagerFactory providedEmf) {
        emf = providedEmf;
        ownsEmf = true;
    }

    public static EntityManager createEntityManager() {
        if (emf == null) throw new IllegalStateException("JPA not initialized");
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (ownsEmf && emf != null && emf.isOpen()) {
            emf.close();
        }
        logger.info("JPA shut down");
    }
}
