package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.JudgeRequestCategory;
import net.deckserver.game.enums.JudgeRequestStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.storage.json.game.JudgeRequestData;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.PlayerInfo;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(PostgresJpaExtension.class)
class JudgeRequestRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    JudgeRequestRepository repo;

    static final String GAME_A_ID = "01ARZ3NDEKTSV4RRFFQ69G5FA";
    static final String GAME_A = "JudgeGameA";
    static final String GAME_B_ID = "01ARZ3NDEKTSV4RRFFQ69G5FB";
    static final String GAME_B = "JudgeGameB";
    static final String OWNER = "judge-owner";

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
        EntityManager setup = emf.createEntityManager();
        setup.getTransaction().begin();
        new PlayerRepository().save(setup, new PlayerInfo(OWNER, "judge-owner-id", "jo@test.com", "hash"));
        GameInfoRepository games = new GameInfoRepository();
        games.save(setup, new GameInfo(GAME_A, GAME_A_ID, OWNER, Visibility.PUBLIC, GameStatus.ACTIVE, GameFormat.STANDARD));
        games.save(setup, new GameInfo(GAME_B, GAME_B_ID, OWNER, Visibility.PUBLIC, GameStatus.ACTIVE, GameFormat.STANDARD));
        setup.getTransaction().commit();
        setup.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        repo = new JudgeRequestRepository();
        // Clean any rows from a previous test in this class.
        em.createQuery("DELETE FROM JudgeRequestEntity").executeUpdate();
        em.flush();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    private JudgeRequestData open(String gameId, String gameName) {
        return repo.insert(em, gameId, gameName, null, OWNER, JudgeRequestCategory.CARD_RULING, "[Fame] question", "parsed");
    }

    @Test
    void insertCreatesOpenRow() {
        JudgeRequestData d = open(GAME_A_ID, GAME_A);
        assertThat(d.getId(), notNullValue());
        assertThat(d.getStatus(), is(JudgeRequestStatus.OPEN));
        assertThat(d.getCreatedAt(), notNullValue());
        assertThat(repo.findOpenForGame(em, GAME_A_ID).getId(), is(d.getId()));
    }

    @Test
    void onlyOneOpenRequestPerGame() {
        open(GAME_A_ID, GAME_A);
        em.flush();
        assertThrows(Exception.class, () -> {
            open(GAME_A_ID, GAME_A);
            em.flush();
        });
    }

    @Test
    void retractFreesTheGameForANewRequest() {
        JudgeRequestData first = open(GAME_A_ID, GAME_A);
        em.flush();

        assertThat(repo.retract(em, first.getId()), is(1));
        em.flush();
        assertThat(repo.findOpenForGame(em, GAME_A_ID), nullValue());

        // A second OPEN request is now allowed.
        open(GAME_A_ID, GAME_A);
        em.flush();
        assertThat(repo.findOpenForGame(em, GAME_A_ID), notNullValue());
    }

    @Test
    void editUpdatesDetailsWhileOpen() {
        JudgeRequestData d = open(GAME_A_ID, GAME_A);
        em.flush();

        assertThat(repo.updateDetails(em, d.getId(), JudgeRequestCategory.INCORRECT_PLAY, "new raw", "new parsed"), is(1));
        em.flush();
        em.clear();

        JudgeRequestData reloaded = repo.findById(em, d.getId());
        assertThat(reloaded.getCategory(), is(JudgeRequestCategory.INCORRECT_PLAY));
        assertThat(reloaded.getRawDetails(), is("new raw"));
        assertThat(reloaded.getParsedDetails(), is("new parsed"));
    }

    @Test
    void editIsRejectedOnceResolved() {
        JudgeRequestData d = open(GAME_A_ID, GAME_A);
        em.flush();
        repo.resolve(em, d.getId(), "judgeX", "raw", "parsed");
        em.flush();

        assertThat(repo.updateDetails(em, d.getId(), JudgeRequestCategory.OTHER, "x", "y"), is(0));
    }

    @Test
    void firstResolveWins() {
        JudgeRequestData d = open(GAME_A_ID, GAME_A);
        em.flush();

        assertThat(repo.resolve(em, d.getId(), "judgeX", "ruling", "ruling parsed"), is(1));
        em.flush();
        assertThat(repo.resolve(em, d.getId(), "judgeY", "other", "other parsed"), is(0));

        em.clear();
        JudgeRequestData reloaded = repo.findById(em, d.getId());
        assertThat(reloaded.getStatus(), is(JudgeRequestStatus.RESOLVED));
        assertThat(reloaded.getResolvedBy(), is("judgeX"));
        assertThat(reloaded.getResolvedAt(), notNullValue());
        assertThat(reloaded.getResolutionParsed(), is("ruling parsed"));
    }

    @Test
    void listOpenIsOldestFirst() throws InterruptedException {
        JudgeRequestData a = open(GAME_A_ID, GAME_A);
        em.flush();
        Thread.sleep(5);
        JudgeRequestData b = open(GAME_B_ID, GAME_B);
        em.flush();
        em.clear();

        List<JudgeRequestData> openList = repo.listOpen(em);
        assertThat(openList, hasSize(2));
        assertThat(openList.get(0).getId(), is(a.getId()));
        assertThat(openList.get(1).getId(), is(b.getId()));
    }

    @Test
    void listResolvedIsNewestFirstAndCountsOnlyOpen() throws InterruptedException {
        JudgeRequestData a = open(GAME_A_ID, GAME_A);
        em.flush();
        repo.resolve(em, a.getId(), "judgeX", "r1", "r1p");
        em.flush();
        Thread.sleep(5);
        JudgeRequestData b = open(GAME_B_ID, GAME_B);
        em.flush();
        repo.resolve(em, b.getId(), "judgeX", "r2", "r2p");
        em.flush();
        em.clear();

        List<JudgeRequestData> history = repo.listResolved(em, 10);
        assertThat(history, hasSize(2));
        assertThat(history.get(0).getId(), is(b.getId()));
        assertThat(repo.countOpen(em), is(0L));
    }

    @Test
    void abandonOpenForGameRetractsWithoutDeleting() {
        JudgeRequestData d = open(GAME_A_ID, GAME_A);
        em.flush();

        repo.abandonOpenForGame(em, GAME_A_ID);
        em.flush();
        em.clear();

        JudgeRequestData reloaded = repo.findById(em, d.getId());
        assertThat(reloaded, notNullValue());
        assertThat(reloaded.getStatus(), is(JudgeRequestStatus.RETRACTED));
    }
}
