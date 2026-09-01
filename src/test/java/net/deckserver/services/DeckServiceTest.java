package net.deckserver.services;

import net.deckserver.storage.json.deck.ExtendedDeck;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class DeckServiceTest {

    private static final String REAL_GAME = "01K6CP9GMWMG78RERJVA2QM0R3";
    private static final String REAL_GAME_DECK = "01GR3EV0H7W4RWRZJ7XZK3RVRY";
    private static final String REAL_MASTER_DECK = "01GR3EV0H7W4RWRZJ7XZK3RVRY";

    private Path createdGameDir;

    @AfterEach
    void cleanUp() throws IOException {
        if (createdGameDir != null && Files.exists(createdGameDir)) {
            try (var paths = Files.walk(createdGameDir)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    @Test
    void gameDeckExists() {
        assertTrue(DeckService.gameDeckExists(REAL_GAME, REAL_GAME_DECK));
        assertFalse(DeckService.gameDeckExists(REAL_GAME, "does-not-exist"));
        assertFalse(DeckService.gameDeckExists("no-such-game", REAL_GAME_DECK));
    }

    @Test
    void loadGameDeckReturnsDeck() {
        ExtendedDeck deck = DeckService.loadGameDeck(REAL_GAME, REAL_GAME_DECK);
        assertNotNull(deck.getDeck());
        assertFalse(deck.getDeck().getCrypt().getCards().isEmpty());
        assertFalse(deck.getDeck().getLibrary().getCards().isEmpty());
    }

    @Test
    void loadGameDeckThrowsWhenMissing() {
        assertThrows(IllegalStateException.class, () -> DeckService.loadGameDeck(REAL_GAME, "does-not-exist"));
        assertThrows(IllegalStateException.class, () -> DeckService.loadGameDeck("no-such-game", REAL_GAME_DECK));
    }

    @Test
    void copyDeckFailsWhenSourceMissing() {
        String gameId = UUID.randomUUID().toString();
        createdGameDir = DataPaths.path("games", gameId);
        assertFalse(DeckService.copyDeck("deck-that-does-not-exist", gameId));
        assertFalse(DeckService.gameDeckExists(gameId, "deck-that-does-not-exist"));
    }

    @Test
    void copyDeckCreatesGameDirAndVerifiesTarget() {
        String gameId = UUID.randomUUID().toString();
        createdGameDir = DataPaths.path("games", gameId);
        assertTrue(DeckService.copyDeck(REAL_MASTER_DECK, gameId));
        assertTrue(DeckService.gameDeckExists(gameId, REAL_MASTER_DECK));
        assertNotNull(DeckService.loadGameDeck(gameId, REAL_MASTER_DECK).getDeck());
    }
}
