package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import net.deckserver.game.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Phase;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jpa.entity.GameChatEntity;
import net.deckserver.jpa.entity.GameStateEntity;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import net.deckserver.storage.json.game.TurnData;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.PlayerInfo;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(PostgresJpaExtension.class)
class GameStateRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    GameStateRepository stateRepo;
    GameChatRepository chatRepo;

    static final String GAME_ID = "01ARZ3NDEKTSV4RRFFQ69G5FA";
    static final String GAME_NAME = "TestGame";
    static final String OWNER = "owner";

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
        // Seed player + game rows required by FK constraints
        EntityManager setup = emf.createEntityManager();
        setup.getTransaction().begin();
        PlayerRepository playerRepo = new PlayerRepository();
        playerRepo.save(setup, new PlayerInfo(OWNER, "owner-id-1", "owner@test.com", "hash"));
        GameInfoRepository gameInfoRepo = new GameInfoRepository();
        gameInfoRepo.save(setup, new GameInfo(GAME_NAME, GAME_ID, OWNER, Visibility.PUBLIC, GameStatus.ACTIVE, GameFormat.STANDARD));
        setup.getTransaction().commit();
        setup.close();
    }

    @AfterAll
    static void tearDownEmf() {
        /* shared EMF: closed by PostgresJpaExtension, not per-class */;
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        stateRepo = new GameStateRepository();
        chatRepo = new GameChatRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    private JolGame makeGame(String phase) {
        GameData data = new GameData(GAME_ID, GAME_NAME);
        data.setTurn("2.1");
        data.setPhase(Phase.valueOf(phase));
        PlayerData p = new PlayerData();
        p.setName(OWNER);
        data.addPlayer(p);
        data.setCurrentPlayer(p);
        return new JolGame(GAME_ID, data);
    }

    @Test
    void saveAndFindGameState() {
        JolGame game = makeGame("MINION");
        stateRepo.save(em, game);
        em.flush();
        em.clear();

        GameStateEntity found = em.find(GameStateEntity.class, GAME_ID);
        assertThat(found, notNullValue());
        assertThat(found.getTurn(), is("2.1"));
        assertThat(found.getPhase(), is("MINION"));
        assertThat(found.getCurrentPlayer(), is(OWNER));
        assertThat(found.getPlayerCount(), is(1));
    }

    @Test
    void loadDeserializesGameData() {
        JolGame game = makeGame("INFLUENCE");
        stateRepo.save(em, game);
        em.flush();
        em.clear();

        GameData loaded = stateRepo.load(em, GAME_ID);
        assertThat(loaded, notNullValue());
        assertThat(loaded.getId(), is(GAME_ID));
        assertThat(loaded.getTurn(), is("2.1"));
        assertThat(loaded.getPhase(), is(Phase.INFLUENCE));
    }

    @Test
    void updateGameState() {
        stateRepo.save(em, makeGame("MINION"));
        em.flush();

        GameData updated = makeGame("INFLUENCE").data();
        updated.setTurn("3.1");
        stateRepo.save(em, new JolGame(GAME_ID, updated));
        em.flush();
        em.clear();

        GameStateEntity found = em.find(GameStateEntity.class, GAME_ID);
        assertThat(found.getTurn(), is("3.1"));
        assertThat(found.getPhase(), is("INFLUENCE"));
    }

    @Test
    void deleteGameState() {
        stateRepo.save(em, makeGame("MINION"));
        em.flush();

        stateRepo.delete(em, GAME_ID);
        em.flush();
        em.clear();

        assertThat(em.find(GameStateEntity.class, GAME_ID), nullValue());
    }

    @Test
    void saveChatHistory() {
        // game_state must exist before chat (FK)
        stateRepo.save(em, makeGame("MINION"));
        em.flush();

        TurnData turn = new TurnData(OWNER, "1.1");
        chatRepo.save(em, GAME_ID, List.of(turn));
        em.flush();
        em.clear();

        GameChatEntity found = em.find(GameChatEntity.class, GAME_ID);
        assertThat(found, notNullValue());
        assertThat(found.getHistory(), containsString(OWNER));
    }

    @Test
    void loadChatHistory() {
        stateRepo.save(em, makeGame("MINION"));
        em.flush();

        TurnData t1 = new TurnData(OWNER, "1.1");
        TurnData t2 = new TurnData(OWNER, "2.1");
        chatRepo.save(em, GAME_ID, List.of(t1, t2));
        em.flush();
        em.clear();

        List<TurnData> turns = chatRepo.load(em, GAME_ID);
        assertThat(turns, hasSize(2));
    }

    @Test
    void deleteChatCascadesWithState() {
        stateRepo.save(em, makeGame("MINION"));
        em.flush();
        chatRepo.save(em, GAME_ID, List.of(new TurnData(OWNER, "1.1")));
        em.flush();

        // Must delete chat before state (explicit, matching repository.delete order)
        chatRepo.delete(em, GAME_ID);
        stateRepo.delete(em, GAME_ID);
        em.flush();
        em.clear();

        assertThat(em.find(GameStateEntity.class, GAME_ID), nullValue());
        assertThat(em.find(GameChatEntity.class, GAME_ID), nullValue());
    }

    @Test
    void loadMissingStateReturnsNull() {
        assertThat(stateRepo.load(em, "nonexistent-id"), nullValue());
    }

    @Test
    void loadMissingChatReturnsEmptyList() {
        Collection<TurnData> result = chatRepo.load(em, "nonexistent-id");
        assertThat(result, empty());
    }
}
