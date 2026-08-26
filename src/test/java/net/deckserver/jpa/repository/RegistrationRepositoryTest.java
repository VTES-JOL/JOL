package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.storage.json.system.GameInfo;
import net.deckserver.storage.json.system.PlayerInfo;
import net.deckserver.storage.json.system.RegistrationStatus;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class RegistrationRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    RegistrationRepository repository;

    @BeforeAll
    static void setUpEmf() {
        emf = Persistence.createEntityManagerFactory("jol-repo-test-pu");
        EntityManager seed = emf.createEntityManager();
        seed.getTransaction().begin();
        PlayerRepository playerRepo = new PlayerRepository();
        playerRepo.save(seed, new PlayerInfo("owner", UUID.randomUUID().toString(), "owner@example.com", "hash"));
        seed.getTransaction().commit();

        seed.getTransaction().begin();
        GameInfoRepository gameRepo = new GameInfoRepository();
        for (String name : List.of("Game1", "Game2", "Game3", "Game4", "Game5")) {
            gameRepo.save(seed, new GameInfo(name, UUID.randomUUID().toString(), "owner", Visibility.PUBLIC, GameStatus.STARTING, GameFormat.STANDARD));
        }
        seed.getTransaction().commit();

        seed.getTransaction().begin();
        for (String name : List.of("Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7")) {
            new PlayerRepository().save(seed, new PlayerInfo(name, UUID.randomUUID().toString(), name + "@example.com", "hash"));
        }
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
        repository = new RegistrationRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    private RegistrationStatus invite() {
        return new RegistrationStatus(OffsetDateTime.now());
    }

    private RegistrationStatus withDeck(String deckId) {
        RegistrationStatus s = new RegistrationStatus(deckId);
        s.setDeckName("My Deck");
        s.setSummary("Crypt: 12 Library: 90");
        return s;
    }

    @Test
    void saveInviteAndFind() {
        repository.save(em, "Game1", "Player1", invite());
        em.flush();
        em.clear();

        List<net.deckserver.jpa.entity.RegistrationEntity> rows = repository.findAllForGame(em, "Game1");

        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).getPlayerName(), is("Player1"));
        assertThat(rows.get(0).toRegistrationStatus().getDeckId(), nullValue());
    }

    @Test
    void registerDeck() {
        String deckId = UUID.randomUUID().toString();
        repository.save(em, "Game2", "Player2", withDeck(deckId));
        em.flush();
        em.clear();

        List<net.deckserver.jpa.entity.RegistrationEntity> rows = repository.findAllForGame(em, "Game2");
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).toRegistrationStatus().getDeckId(), is(deckId));
        assertThat(rows.get(0).toRegistrationStatus().getSummary(), is("Crypt: 12 Library: 90"));
    }

    @Test
    void updateFromInviteToDeckRegistration() {
        repository.save(em, "Game3", "Player3", invite());
        em.flush();

        String deckId = UUID.randomUUID().toString();
        repository.save(em, "Game3", "Player3", withDeck(deckId));
        em.flush();
        em.clear();

        List<net.deckserver.jpa.entity.RegistrationEntity> rows = repository.findAllForGame(em, "Game3");
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).toRegistrationStatus().getDeckId(), is(deckId));
    }

    @Test
    void deletePlayer() {
        repository.save(em, "Game4", "Player4", invite());
        repository.save(em, "Game4", "Player5", invite());
        em.flush();

        repository.delete(em, "Game4", "Player4");
        em.flush();
        em.clear();

        List<net.deckserver.jpa.entity.RegistrationEntity> rows = repository.findAllForGame(em, "Game4");
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).getPlayerName(), is("Player5"));
    }

    @Test
    void clearAllForGame() {
        repository.save(em, "Game5", "Player6", invite());
        repository.save(em, "Game5", "Player7", invite());
        em.flush();

        repository.deleteAllForGame(em, "Game5");
        em.flush();
        em.clear();

        assertThat(repository.findAllForGame(em, "Game5"), empty());
    }
}
