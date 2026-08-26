package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.PlayerInfo;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GameInfoRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    GameInfoRepository repository;
    PlayerRepository playerRepository;

    @BeforeAll
    static void setUpEmf() {
        emf = Persistence.createEntityManagerFactory("jol-repo-test-pu");
        // Seed the owner player once — persists for the lifetime of the in-memory DB
        EntityManager seed = emf.createEntityManager();
        seed.getTransaction().begin();
        new PlayerRepository().save(seed, new PlayerInfo("owner", UUID.randomUUID().toString(), "owner@example.com", "hash"));
        seed.getTransaction().commit();
        seed.close();
    }

    @AfterAll
    static void tearDownEmf() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        repository = new GameInfoRepository();
        playerRepository = new PlayerRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    private GameInfo makeGame(String name) {
        return new GameInfo(name, UUID.randomUUID().toString(), "owner", Visibility.PUBLIC, GameStatus.STARTING, GameFormat.STANDARD);
    }

    @Test
    void saveAndFindGame() {
        GameInfo game = makeGame("TestGame");

        repository.save(em, game);
        em.flush();
        em.clear();

        Map<String, GameInfo> all = repository.findAll(em);

        assertThat(all, hasKey("TestGame"));
        GameInfo found = all.get("TestGame");
        assertThat(found.getStatus(), is(GameStatus.STARTING));
        assertThat(found.getGameFormat(), is(GameFormat.STANDARD));
        assertThat(found.getVisibility(), is(Visibility.PUBLIC));
    }

    @Test
    void updateGameStatus() {
        GameInfo game = makeGame("StatusGame");
        repository.save(em, game);
        em.flush();

        game.setStatus(GameStatus.ACTIVE);
        repository.save(em, game);
        em.flush();
        em.clear();

        GameInfo found = repository.findAll(em).get("StatusGame");
        assertThat(found.getStatus(), is(GameStatus.ACTIVE));
    }

    @Test
    void deleteGame() {
        GameInfo game = makeGame("DeleteGame");
        repository.save(em, game);
        em.flush();

        repository.delete(em, "DeleteGame");
        em.flush();
        em.clear();

        assertThat(repository.findAll(em), not(hasKey("DeleteGame")));
    }

    @Test
    void versionRoundTrip() {
        GameInfo game = makeGame("VersionGame");
        game.setVersion(GameInfo.Version.DATA_FIX);
        repository.save(em, game);
        em.flush();
        em.clear();

        GameInfo found = repository.findAll(em).get("VersionGame");
        assertThat(found.getVersion(), is(GameInfo.Version.DATA_FIX));
    }

    @Test
    void tournamentNameRoundTrip() {
        GameInfo game = makeGame("TournamentGame");
        game.setTournamentName("Grand Tournament");
        repository.save(em, game);
        em.flush();
        em.clear();

        GameInfo found = repository.findAll(em).get("TournamentGame");
        assertThat(found.getTournamentName(), is("Grand Tournament"));
    }
}
