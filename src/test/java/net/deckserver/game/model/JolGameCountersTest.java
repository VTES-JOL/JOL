package net.deckserver.game.model;

import net.deckserver.game.enums.RegionType;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Bug: {@code changeCounters}/{@code transfer} let a minion's blood counter go
 * negative — e.g. "move 1 blood off a minion that already has 0". Both must
 * floor at 0 instead of trusting the caller's delta.
 */
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class JolGameCountersTest {

    private static JolGame newGame() {
        GameData data = new GameData("g1", "Test Game");
        PlayerData player = new PlayerData("Player1");
        data.addPlayer(player);
        CardData minion = new CardData("100001", player);
        minion.setCounters(0);
        player.getRegion(RegionType.READY).addCard(minion, false);
        data.getCards().put(minion.getId(), minion);
        return new JolGame("g1", data);
    }

    @Test
    void changeCountersDoesNotGoNegative() {
        JolGame game = newGame();
        CardData minion = game.data().getCards().values().iterator().next();
        assertEquals(0, minion.getCounters());

        // Removing blood from a minion that already has 0 must not underflow.
        game.changeCounters("Player1", minion.getId(), -1, true);

        assertEquals(0, minion.getCounters(), "blood counter must not go negative");
    }

    @Test
    void transferOffCardDoesNotGoNegative() {
        JolGame game = newGame();
        CardData minion = game.data().getCards().values().iterator().next();
        assertEquals(0, minion.getCounters());

        // Moving blood off the card back to the controller's pool with none
        // present must not underflow the counter (or overcredit the pool).
        int startingPool = game.data().getPlayer("Player1").getPool();
        game.transfer("Player1", minion.getId(), -1);

        assertEquals(0, minion.getCounters(), "blood counter must not go negative");
        assertEquals(startingPool, game.data().getPlayer("Player1").getPool(), "pool must not gain blood that was never on the card");
    }
}
