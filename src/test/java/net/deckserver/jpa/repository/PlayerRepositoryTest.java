package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.storage.json.system.PlayerInfo;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class PlayerRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    PlayerRepository repository;

    @BeforeAll
    static void setUpEmf() {
        emf = Persistence.createEntityManagerFactory("jol-repo-test-pu");
    }

    @AfterAll
    static void tearDownEmf() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        repository = new PlayerRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    @Test
    void saveAndFindPlayer() {
        PlayerInfo player = new PlayerInfo("TestPlayer", UUID.randomUUID().toString(), "test@example.com", "hash123");
        player.setCountryCode("AU");

        repository.save(em, player);
        em.flush();
        em.clear();

        PlayerInfo found = repository.findByName(em, "TestPlayer");

        assertThat(found, notNullValue());
        assertThat(found.getName(), is("TestPlayer"));
        assertThat(found.getEmail(), is("test@example.com"));
        assertThat(found.getCountryCode(), is("AU"));
    }

    @Test
    void savePlayerWithRoles() {
        PlayerInfo player = new PlayerInfo("AdminPlayer", UUID.randomUUID().toString(), "admin@example.com", "hash");
        player.setRoles(Set.of(PlayerRole.ADMIN, PlayerRole.JUDGE));

        repository.save(em, player);
        em.flush();
        em.clear();

        PlayerInfo found = repository.findByName(em, "AdminPlayer");

        assertThat(found, notNullValue());
        assertThat(found.getRoles(), containsInAnyOrder(PlayerRole.ADMIN, PlayerRole.JUDGE));
    }

    @Test
    void updatePlayer() {
        PlayerInfo player = new PlayerInfo("UpdatePlayer", UUID.randomUUID().toString(), "old@example.com", "hash");
        repository.save(em, player);
        em.flush();

        player.setEmail("new@example.com");
        player.setDiscordId("discord123");
        repository.save(em, player);
        em.flush();
        em.clear();

        PlayerInfo found = repository.findByName(em, "UpdatePlayer");
        assertThat(found.getEmail(), is("new@example.com"));
        assertThat(found.getDiscordId(), is("discord123"));
    }

    @Test
    void deletePlayer() {
        PlayerInfo player = new PlayerInfo("DeletePlayer", UUID.randomUUID().toString(), "del@example.com", "hash");
        repository.save(em, player);
        em.flush();

        repository.delete(em, "DeletePlayer");
        em.flush();
        em.clear();

        assertThat(repository.findByName(em, "DeletePlayer"), nullValue());
    }

    @Test
    void findAll() {
        repository.save(em, new PlayerInfo("FindAll1", UUID.randomUUID().toString(), "a@example.com", "hash"));
        repository.save(em, new PlayerInfo("FindAll2", UUID.randomUUID().toString(), "b@example.com", "hash"));
        em.flush();
        em.clear();

        Map<String, PlayerInfo> all = repository.findAll(em);

        assertThat(all, hasKey("FindAll1"));
        assertThat(all, hasKey("FindAll2"));
    }

    @Test
    void findNonExistentPlayerReturnsNull() {
        assertThat(repository.findByName(em, "NoSuchPlayer"), nullValue());
    }
}
