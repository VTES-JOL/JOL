package net.deckserver.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.deckserver.jpa.JpaFactory;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JUnit 5 extension that initialises JpaFactory with H2 and populates fixture data
 * before any service singleton is first accessed.
 *
 * Usage: add @ExtendWith(JolServiceExtension.class) to service-level test classes.
 * Keep @SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true") to suppress
 * scheduled persistence and jpaWrite calls.
 */
public class JolServiceExtension implements BeforeAllCallback {

    private static final Logger logger = LoggerFactory.getLogger(JolServiceExtension.class);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ensureInitialized();
    }

    public static void ensureInitialized() throws Exception {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jol-test-pu");
        JpaFactory.initializeWithEmf(emf);
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            new JolFixtureLoader(em).load();
            em.getTransaction().commit();
            logger.info("JolServiceExtension: H2 fixture data loaded");
        } catch (Exception e) {
            logger.error("JolServiceExtension: fixture load failed", e);
            throw e;
        }
    }
}
