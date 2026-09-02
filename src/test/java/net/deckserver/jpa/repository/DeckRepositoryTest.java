package net.deckserver.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.deckserver.testsupport.PostgresJpaExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.jpa.entity.DeckContentEntity;
import net.deckserver.jpa.entity.DeckInfoEntity;
import net.deckserver.jpa.entity.DeckInfoId;
import net.deckserver.storage.json.deck.*;
import net.deckserver.storage.json.system.DeckInfo;
import net.deckserver.storage.json.system.PlayerInfo;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(PostgresJpaExtension.class)
class DeckRepositoryTest {

    static EntityManagerFactory emf;
    EntityManager em;
    DeckRepository repository;

    static String PLAYER1_ID;

    @BeforeAll
    static void setUpEmf() {
        emf = PostgresJpaExtension.emf();
        PLAYER1_ID = UUID.randomUUID().toString();
        EntityManager seed = emf.createEntityManager();
        seed.getTransaction().begin();
        new PlayerRepository().save(seed, new PlayerInfo("Player1", PLAYER1_ID, "p1@example.com", "hash"));
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
        repository = new DeckRepository();
    }

    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    private DeckInfo makeDeckInfo(String deckId) {
        return new DeckInfo(deckId, "Test Deck", DeckFormat.TAGGED, Set.of("STANDARD"));
    }

    private ExtendedDeck makeExtendedDeck() {
        Deck deck = new Deck();
        deck.setName("Test Deck");
        deck.setAuthor("Player1");
        Crypt crypt = new Crypt();
        crypt.setCount(12);
        deck.setCrypt(crypt);
        Library library = new Library();
        library.setCount(90);
        deck.setLibrary(library);
        ExtendedDeck extended = new ExtendedDeck();
        extended.setDeck(deck);
        return extended;
    }

    @Test
    void saveDeckInfoAndFind() {
        String deckId = UUID.randomUUID().toString();
        DeckInfo info = makeDeckInfo(deckId);

        repository.saveDeckInfo(em, "Player1", "Deck1", info);
        em.flush();
        em.clear();

        List<DeckInfoEntity> all = repository.findAllDeckInfos(em);
        assertThat(all, hasSize(1));
        DeckInfo found = all.get(0).toDeckInfo();
        assertThat(found.getDeckId(), is(deckId));
        assertThat(found.getFormat(), is(DeckFormat.TAGGED));
        assertThat(found.getGameFormats(), contains("STANDARD"));
    }

    @Test
    void saveDeckContent() {
        String deckId = UUID.randomUUID().toString();
        repository.saveDeckInfo(em, "Player1", "ContentDeck", makeDeckInfo(deckId));
        em.flush();

        repository.saveContent(em, deckId, makeExtendedDeck());
        em.flush();
        em.clear();

        DeckContentEntity content = em.find(DeckContentEntity.class, deckId);
        assertThat(content, notNullValue());
        assertThat(content.getContent(), containsString("Test Deck"));
    }

    @Test
    void updateDeckInfo() {
        String deckId = UUID.randomUUID().toString();
        DeckInfo info = makeDeckInfo(deckId);
        repository.saveDeckInfo(em, "Player1", "UpdateDeck", info);
        em.flush();

        info.setFormat(DeckFormat.MODERN);
        repository.saveDeckInfo(em, "Player1", "UpdateDeck", info);
        em.flush();
        em.clear();

        DeckInfoEntity found = em.find(DeckInfoEntity.class, new DeckInfoId(PLAYER1_ID, "UpdateDeck"));
        assertThat(found.toDeckInfo().getFormat(), is(DeckFormat.MODERN));
    }

    @Test
    void deleteDeckCascadesToContent() {
        String deckId = UUID.randomUUID().toString();
        repository.saveDeckInfo(em, "Player1", "DeleteDeck", makeDeckInfo(deckId));
        em.flush();
        repository.saveContent(em, deckId, makeExtendedDeck());
        em.flush();

        repository.delete(em, "Player1", "DeleteDeck");
        em.flush();
        em.clear();

        assertThat(repository.findAllDeckInfos(em), empty());
        assertThat(em.find(DeckContentEntity.class, deckId), nullValue());
    }

    @Test
    void saveContentWritesKrcgV5Shape() {
        String deckId = UUID.randomUUID().toString();
        repository.saveDeckInfo(em, "Player1", "V5Deck", makeDeckInfo(deckId));
        em.flush();

        ExtendedDeck deck = DeckParser.parseDeck("4 Nkechi\n\n10 Govern the Unaligned\n");
        repository.saveContent(em, deckId, deck);
        em.flush();
        em.clear();

        String stored = em.find(DeckContentEntity.class, deckId).getContent();
        assertThat(stored, containsString("\"cards\""));
        assertThat(stored, containsString("\"kind\""));
        assertThat(stored, not(containsString("\"crypt\"")));

        ExtendedDeck back = repository.findContent(em, deckId);
        assertThat(back.getErrors(), is(empty()));
        assertThat(back.getDeck().getCrypt().getCount(), is(4));
        assertThat(back.getDeck().getLibrary().getCount(), is(10));
    }

    @Test
    void updateDeckContent() {
        String deckId = UUID.randomUUID().toString();
        repository.saveDeckInfo(em, "Player1", "ContentUpdate", makeDeckInfo(deckId));
        repository.saveContent(em, deckId, makeExtendedDeck());
        em.flush();

        ExtendedDeck updated = makeExtendedDeck();
        updated.getDeck().setName("Updated Name");
        repository.saveContent(em, deckId, updated);
        em.flush();
        em.clear();

        DeckContentEntity content = em.find(DeckContentEntity.class, deckId);
        assertThat(content.getContent(), containsString("Updated Name"));
    }
}
