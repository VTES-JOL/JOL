package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.jpa.entity.DeckInfoEntity;
import net.deckserver.storage.json.system.PlayerInfo;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Asserts that the declarative fixture (src/main/resources/db/testseed/
 * V900__test_fixture.sql) is present and readable through the repositories -
 * i.e. that the "fixed set of test data" the JPA tier runs against actually
 * loaded, on the real migrated Postgres schema.
 */
@ExtendWith(PostgresJpaExtension.class)
class FixtureDataTest {

    static EntityManagerFactory emf;
    EntityManager em;

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
        // The extension resets every class to a bare migrated schema; this test
        // is the one that wants the db/testseed fixture on top.
        PostgresJpaExtension.applyTestSeed();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    @Test
    void fixturePlayersAreLoaded() {
        Map<String, PlayerInfo> all = new PlayerRepository().findAll(em);

        assertThat(all, hasKey("fixture-alice"));
        assertThat(all, hasKey("fixture-bob"));
        assertThat(all, hasKey("fixture-carol"));
        assertThat(all.get("fixture-alice").getEmail(), is("alice@fixture.test"));
    }

    @Test
    void fixtureJudgeRoleIsLoaded() {
        PlayerInfo alice = new PlayerRepository().findByName(em, "fixture-alice");

        assertThat(alice, notNullValue());
        assertThat(alice.getRoles(), hasItem(PlayerRole.JUDGE));
    }

    @Test
    void fixtureDecksAreLoaded() {
        List<DeckInfoEntity> decks = new DeckRepository().findAllDeckInfos(em).stream()
                .filter(d -> d.toDeckInfo().getDeckId().startsWith("f0000000-0000-0000-0000-0000000000d"))
                .toList();

        assertThat(decks, hasSize(2));
    }

    @Test
    void schemaResetGivesEachClassTheSameBaseline() {
        // Each class gets flyway clean + migrate, so no rows committed by another
        // *RepositoryTest's @BeforeAll leak in. Only migration-seeded rows
        // (SYSTEM, from V14) plus this fixture are present.
        var names = new PlayerRepository().findAll(em).keySet();
        assertThat(names, hasItems("fixture-alice", "fixture-bob", "fixture-carol"));
        assertThat(names, everyItem(anyOf(
                is("SYSTEM"), startsWith("fixture-"))));
    }
}
