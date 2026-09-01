package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;
import net.deckserver.jpa.entity.TournamentEntity;
import net.deckserver.storage.json.system.*;
import org.junit.jupiter.api.*;

import net.deckserver.storage.json.system.PlayerInfo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(PostgresJpaExtension.class)
class TournamentRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    TournamentRepository repository;

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
        EntityManager seed = emf.createEntityManager();
        seed.getTransaction().begin();
        PlayerRepository playerRepo = new PlayerRepository();
        for (String name : List.of("Player1", "Player2")) {
            playerRepo.save(seed, new PlayerInfo(name, UUID.randomUUID().toString(), name + "@example.com", "hash"));
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
        repository = new TournamentRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    private TournamentDefinition makeTournament(String name) {
        TournamentDefinition def = new TournamentDefinition();
        def.setName(name);
        def.setFormat(TournamentFormat.SINGLE_DECK);
        def.setDeckFormat(GameFormat.STANDARD);
        def.setNumberOfRounds(3);
        def.setStatus(GameStatus.EDIT);
        OffsetDateTime now = OffsetDateTime.now();
        def.setRegistrationStart(now.minusDays(1));
        def.setRegistrationEnd(now.plusDays(7));
        def.setPlayStarts(now.plusDays(7));
        def.setPlayEnds(now.plusDays(30));
        return def;
    }

    @Test
    void saveAndFindTournament() {
        TournamentDefinition def = makeTournament("Grand Prix");
        repository.save(em, def);
        em.flush();
        em.clear();

        List<TournamentEntity> all = repository.findAll(em);
        assertThat(all, hasSize(1));
        assertThat(all.get(0).getName(), is("Grand Prix"));
        assertThat(all.get(0).getStatus(), is(GameStatus.EDIT));
    }

    @Test
    void saveWithRegistrations() {
        TournamentDefinition def = makeTournament("Open Series");
        def.getRegistrations().add(new TournamentRegistration("Player1", "12345"));
        def.getRegistrations().add(new TournamentRegistration("Player2", "67890"));
        repository.save(em, def);
        em.flush();
        em.clear();

        List<net.deckserver.jpa.entity.TournamentRegistrationEntity> regs =
                repository.findRegistrations(em, def.getId());
        assertThat(regs, hasSize(2));
        assertThat(regs.stream().map(r -> r.toRegistration().getPlayer()).toList(),
                containsInAnyOrder("Player1", "Player2"));
    }

    @Test
    void updateTournamentStatus() {
        TournamentDefinition def = makeTournament("Status Tour");
        repository.save(em, def);
        em.flush();

        def.setStatus(GameStatus.ACTIVE);
        repository.save(em, def);
        em.flush();
        em.clear();

        TournamentEntity found = repository.findAll(em).get(0);
        assertThat(found.getStatus(), is(GameStatus.ACTIVE));
    }

    @Test
    void roundsSerializationRoundTrip() {
        TournamentDefinition def = makeTournament("Rounds Tour");
        TournamentPlayer p1 = new TournamentPlayer();
        p1.setName("Alpha");
        TournamentPlayer p2 = new TournamentPlayer();
        p2.setName("Beta");
        def.setRounds(Map.of(1, Map.of(1, List.of(p1, p2))));
        repository.save(em, def);
        em.flush();
        em.clear();

        TournamentEntity entity = repository.findAll(em).get(0);
        List<net.deckserver.jpa.entity.TournamentRegistrationEntity> regs =
                repository.findRegistrations(em, def.getId());
        TournamentDefinition restored = repository.toDefinition(entity, regs);

        assertThat(restored.getPlayers(1, 1), hasSize(2));
        assertThat(restored.getPlayers(1, 1).stream().map(TournamentPlayer::getName).toList(),
                containsInAnyOrder("Alpha", "Beta"));
    }

    @Test
    void updateRegistrationsReplacedOnSave() {
        TournamentDefinition def = makeTournament("Reg Replace Tour");
        def.getRegistrations().add(new TournamentRegistration("Player1", "111"));
        repository.save(em, def);
        em.flush();

        // Replace registrations
        def.getRegistrations().clear();
        def.getRegistrations().add(new TournamentRegistration("Player2", "222"));
        repository.save(em, def);
        em.flush();
        em.clear();

        List<net.deckserver.jpa.entity.TournamentRegistrationEntity> regs =
                repository.findRegistrations(em, def.getId());
        assertThat(regs, hasSize(1));
        assertThat(regs.get(0).getPlayerName(), is("Player2"));
    }

    @Test
    void deleteTournament() {
        TournamentDefinition def = makeTournament("Delete Tour");
        def.getRegistrations().add(new TournamentRegistration("Player1", "111"));
        repository.save(em, def);
        em.flush();

        repository.delete(em, def.getId());
        em.flush();
        em.clear();

        assertThat(repository.findAll(em), empty());
        assertThat(repository.findRegistrations(em, def.getId()), empty());
    }
}
