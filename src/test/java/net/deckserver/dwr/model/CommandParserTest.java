package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.storage.json.deck.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
public class CommandParserTest {

    private JolGame game;
    private JolGame game2;

    @BeforeEach
    public void setUp() {
        game = ModelLoader.loadGame("command-test");

        game2 = new JolGame("id", new DsGame(), new DsTurnRecorder());
        game2.addPlayer("ShanDow", new Deck());
        game2.addPlayer("shade", new Deck());
        game2.addPlayer("ShanWod", new Deck());
        game2.addPlayer("Mårten", new Deck());
        game2.addPlayer("Marten", new Deck());
    }

    @Test
    public void testMoveWithPeriods() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"move", "ready", "2.1", "ashheap"}, 1, game);
        String player = "Player1";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("14", card.getId());
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.ASH_HEAP, destinationRegion);
    }

    @Test
    public void testTargetParse() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"move", "ready", "2.1", "ashheap"}, 1, game);
        String player = "Player1";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, false, sourcePlayer, sourceRegion);
        assertEquals("14", card.getId());
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.ASH_HEAP, destinationRegion);
    }

    @Test
    public void addBloodTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"blood", "ready", "1", "+1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, false, sourcePlayer, sourceRegion);
        assertEquals("111", card.getId());
    }

    @Test
    public void addBloodAttachedCardTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"blood", "ready", "1", "1", "+1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, false, sourcePlayer, sourceRegion);
        assertEquals("111", card.getId());
    }

    @Test
    public void addBloodNonGreedyTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"blood", "ready", "1", "1", "1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, false, sourcePlayer, sourceRegion);
        assertEquals("111", card.getId());
    }

    @Test
    public void addBloodNewFormatNonGreedyTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"blood", "ready", "1.1", "1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, false, sourcePlayer, sourceRegion);
        assertEquals("111", card.getId());
    }

    @Test
    public void addBloodNewFormatTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"blood", "ready", "1.1", "+1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        Card card = commandParser.findCard(false, false, sourcePlayer, sourceRegion);
        assertEquals("111", card.getId());
    }

    @Test
    public void moveCardComplicatedTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"move", "hand", "1", "ready", "1.1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.HAND, sourceRegion);
        Card sourceCard = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("141", sourceCard.getId());
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, destinationRegion);
        Card destinationCard = commandParser.findCard(false, destinationPlayer, destinationRegion);
        assertEquals("111", destinationCard.getId());
    }

    @Test
    public void moveCardOldSyntaxComplicatedTest() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"move", "hand", "1", "ready", "1", "1"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.HAND, sourceRegion);
        Card sourceCard = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("141", sourceCard.getId());
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, destinationRegion);
        Card destinationCard = commandParser.findCard(false, destinationPlayer, destinationRegion);
        assertEquals("111", destinationCard.getId());
    }

    @Test
    public void testDefaultPlayer() throws Exception {
        CommandParser commandParser = new CommandParser(new String[]{"move"}, 1, game);
        String player = commandParser.getPlayer("Marten");
        assertEquals("Marten", player);
    }

    @Test
    public void getPlayerFullMatchTest() throws Exception {
        CommandParser commandParser = new CommandParser(new String[]{"move", "ShanDow", "hand", "1", "ready", "1", "1"}, 1, game2);
        String player = commandParser.getPlayer("Marten");
        assertEquals("ShanDow", player);
    }

    @Test
    public void getPlayerShortestMatch() throws Exception {
        CommandParser commandParser = new CommandParser(new String[]{"move", "Sha", "hand", "1", "ready", "1", "1"}, 1, game2);
        String player = commandParser.getPlayer("Marten");
        assertEquals("shade", player);
    }

    @Test
    public void getPlayerCaseInsensitiveMatch() throws Exception {
        CommandParser commandParser = new CommandParser(new String[]{"move", "shand", "hand", "1", "ready", "1", "1"}, 1, game2);
        String player = commandParser.getPlayer("Marten");
        assertEquals("ShanDow", player);
    }

    @Test
    public void getPlayerAccentMatch() throws CommandException {
        CommandParser commandParser = new CommandParser(new String[]{"move", "Mårt", "hand", "1", "ready", "1", "1"}, 1, game2);
        String player = commandParser.getPlayer("Marten");
        assertEquals("Mårten", player);
    }

    @Test
    public void getPlayerAccentPriority() throws CommandException {
        CommandParser commandParser = new CommandParser(new String[]{"move", "Mårt", "hand", "1", "ready", "1", "1"}, 1, game2);
        String player = commandParser.getPlayer("Marten");
        assertEquals("Mårten", player);
    }

    @Test
    public void getPlayerNotSpecificEnough() throws CommandException {
        CommandParser commandParser = new CommandParser(new String[]{"move", "shan", "hand", "1", "ready", "1", "1"}, 1, game2);
        assertThrows(CommandException.class, () -> commandParser.getPlayer("ShanDow"));
    }
}