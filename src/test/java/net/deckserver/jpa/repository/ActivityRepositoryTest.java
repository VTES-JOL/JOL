package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import net.deckserver.rest.bean.ChatEntryBean;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jpa.entity.GameActivityEntity;
import net.deckserver.jpa.entity.GlobalChatEntity;
import net.deckserver.jpa.entity.PlayerActivityEntity;
import net.deckserver.storage.json.game.GameTimestampEntry;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.PlayerInfo;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(PostgresJpaExtension.class)
class ActivityRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    PlayerActivityRepository playerActivityRepo;
    GameActivityRepository gameActivityRepo;
    GlobalChatRepository globalChatRepo;

    static final String OWNER = "owner";
    static final Map<String, String> GAME_IDS = new java.util.HashMap<>();
    static final Map<String, String> PLAYER_IDS = new java.util.HashMap<>();

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
        EntityManager seed = emf.createEntityManager();
        seed.getTransaction().begin();
        PlayerRepository playerRepo = new PlayerRepository();
        for (String name : List.of(OWNER, "Player1", "Player2", "Alpha", "Beta", "P1", "P2", "P3", "Player")) {
            String id = UUID.randomUUID().toString();
            PLAYER_IDS.put(name, id);
            playerRepo.save(seed, new PlayerInfo(name, id, name + "@example.com", "hash"));
        }
        seed.getTransaction().commit();

        seed.getTransaction().begin();
        GameInfoRepository gameRepo = new GameInfoRepository();
        for (String name : List.of("GameA", "GameB", "GameC", "Game1", "Game2")) {
            String id = UUID.randomUUID().toString();
            GAME_IDS.put(name, id);
            gameRepo.save(seed, new GameInfo(name, id, OWNER, Visibility.PUBLIC, GameStatus.STARTING, GameFormat.STANDARD));
        }
        seed.getTransaction().commit();
        seed.close();
    }

    @AfterAll
    static void tearDownEmf() {
        /* shared EMF: closed by PostgresJpaExtension, not per-class */;
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        playerActivityRepo = new PlayerActivityRepository();
        gameActivityRepo = new GameActivityRepository();
        globalChatRepo = new GlobalChatRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    // --- PlayerActivityRepository ---

    @Test
    void saveAndFindPlayerActivity() {
        OffsetDateTime now = OffsetDateTime.now().withNano(0);
        playerActivityRepo.save(em, "Player1", now);
        em.flush();
        em.clear();

        PlayerActivityEntity found = em.find(PlayerActivityEntity.class, PLAYER_IDS.get("Player1"));
        assertThat(found, notNullValue());
        assertThat(found.getLastSeen().toEpochSecond(), is(now.toEpochSecond()));
    }

    @Test
    void updatePlayerActivity() {
        OffsetDateTime first = OffsetDateTime.now().minusMinutes(10).withNano(0);
        OffsetDateTime second = OffsetDateTime.now().withNano(0);
        playerActivityRepo.save(em, "Player2", first);
        em.flush();

        playerActivityRepo.save(em, "Player2", second);
        em.flush();
        em.clear();

        PlayerActivityEntity found = em.find(PlayerActivityEntity.class, PLAYER_IDS.get("Player2"));
        assertThat(found.getLastSeen().toEpochSecond(), is(second.toEpochSecond()));
    }

    @Test
    void saveAllPlayerTimestamps() {
        OffsetDateTime now = OffsetDateTime.now().withNano(0);
        playerActivityRepo.saveAll(em, Map.of("Alpha", now, "Beta", now));
        em.flush();
        em.clear();

        List<PlayerActivityEntity> all = playerActivityRepo.findAll(em);
        assertThat(all.stream().map(PlayerActivityEntity::getPlayerId).toList(),
                containsInAnyOrder(PLAYER_IDS.get("Alpha"), PLAYER_IDS.get("Beta")));
    }

    // --- GameActivityRepository ---

    @Test
    void saveAndFindGameActivity() {
        GameTimestampEntry entry = new GameTimestampEntry();
        entry.recordPlayerAccess("Player1");
        entry.setPlayerPing("Player1");

        gameActivityRepo.save(em, "GameA", entry);
        em.flush();
        em.clear();

        GameActivityEntity found = em.find(GameActivityEntity.class, GAME_IDS.get("GameA"));
        assertThat(found, notNullValue());
        assertThat(found.getPlayerTimestamps(), containsString("Player1"));
        assertThat(found.getPlayerPings(), containsString("Player1"));
    }

    @Test
    void updateGameActivity() {
        GameTimestampEntry entry = new GameTimestampEntry();
        entry.recordPlayerAccess("Player1");
        gameActivityRepo.save(em, "GameB", entry);
        em.flush();

        entry.recordPlayerAccess("Player2");
        gameActivityRepo.save(em, "GameB", entry);
        em.flush();
        em.clear();

        GameActivityEntity found = em.find(GameActivityEntity.class, GAME_IDS.get("GameB"));
        assertThat(found.getPlayerTimestamps(), containsString("Player2"));
    }

    @Test
    void deleteGameActivity() {
        GameTimestampEntry entry = new GameTimestampEntry();
        gameActivityRepo.save(em, "GameC", entry);
        em.flush();

        gameActivityRepo.delete(em, "GameC");
        em.flush();
        em.clear();

        assertThat(em.find(GameActivityEntity.class, GAME_IDS.get("GameC")), nullValue());
    }

    @Test
    void saveAllGameTimestamps() {
        GameTimestampEntry e1 = new GameTimestampEntry();
        GameTimestampEntry e2 = new GameTimestampEntry();
        gameActivityRepo.saveAll(em, Map.of("Game1", e1, "Game2", e2));
        em.flush();
        em.clear();

        assertThat(em.find(GameActivityEntity.class, GAME_IDS.get("Game1")), notNullValue());
        assertThat(em.find(GameActivityEntity.class, GAME_IDS.get("Game2")), notNullValue());
    }

    // --- GlobalChatRepository ---

    @Test
    void insertAndFindChat() {
        ChatEntryBean chat = new ChatEntryBean("Player1", "Hello world");
        globalChatRepo.insert(em, chat);
        em.flush();
        em.clear();

        List<GlobalChatEntity> recent = globalChatRepo.findRecent(em, 10);
        assertThat(recent, hasSize(1));
        assertThat(recent.get(0).getPlayerName(), is("Player1"));
        assertThat(recent.get(0).getMessage(), is("Hello world"));
    }

    @Test
    void findRecentOrderedDescById() {
        globalChatRepo.insert(em, new ChatEntryBean("P1", "First"));
        globalChatRepo.insert(em, new ChatEntryBean("P2", "Second"));
        globalChatRepo.insert(em, new ChatEntryBean("P3", "Third"));
        em.flush();
        em.clear();

        List<GlobalChatEntity> recent = globalChatRepo.findRecent(em, 2);
        assertThat(recent, hasSize(2));
        // Most recent first
        assertThat(recent.get(0).getMessage(), is("Third"));
        assertThat(recent.get(1).getMessage(), is("Second"));
    }

    @Test
    void trimKeepsOnlyRecentRows() {
        // Insert 5 rows and verify trim to 3 works (using a small constant via subquery logic)
        for (int i = 1; i <= 5; i++) {
            globalChatRepo.insert(em, new ChatEntryBean("Player", "msg" + i));
        }
        em.flush();

        // Trim manually using the cutoff approach with limit=3
        Long cutoffId = em.createQuery(
                        "SELECT c.id FROM GlobalChatEntity c ORDER BY c.id DESC", Long.class)
                .setFirstResult(2) // keep top 3 (index 0,1,2 → cutoff at index 2)
                .setMaxResults(1)
                .getSingleResult();
        em.createQuery("DELETE FROM GlobalChatEntity c WHERE c.id < :cutoff")
                .setParameter("cutoff", cutoffId)
                .executeUpdate();
        em.flush();
        em.clear();

        List<GlobalChatEntity> remaining = globalChatRepo.findRecent(em, 100);
        assertThat(remaining.size(), is(3));
        // The most recent 3 messages are msg5, msg4, msg3
        assertThat(remaining.get(0).getMessage(), is("msg5"));
    }
}
