package net.deckserver.dwr.model;

import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.storage.json.deck.Deck;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommandParserTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private JolGame game;
    private JolGame game2;

    @Before
    public void setUp() {
        environmentVariables.set("JOL_DATA", "src/test/resources/data");
        game = ModelLoader.loadGame("01JHQ7QXHB7SR86F3RNVSXFVMN");

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
        CommandParser commandParser = new CommandParser(new String[]{"move", "ready", "1.1", "ashheap"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        String cardId = commandParser.findCard(sourcePlayer, sourceRegion);
        assertEquals("197", cardId);
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.ASH_HEAP, destinationRegion);
    }

    @Test
    public void testTargetParse() throws Exception {
        assertNotNull(game);
        CommandParser commandParser = new CommandParser(new String[]{"move", "ready", "1.1", "ashheap"}, 1, game);
        String player = "Player2";
        String sourcePlayer = commandParser.getPlayer(player);
        assertEquals(player, sourcePlayer);
        String sourceRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, sourceRegion);
        String cardId = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("197", cardId);
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
        String cardId = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("111", cardId);
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
        String cardId = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("197", cardId);
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
        String cardId = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("197", cardId);
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
        String cardId = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("197", cardId);
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
        String cardId = commandParser.findCard(false, sourcePlayer, sourceRegion);
        assertEquals("197", cardId);
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
        String sourceCard = commandParser.findCard(sourcePlayer, sourceRegion);
        assertEquals("141", sourceCard);
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, destinationRegion);
        String destinationCard = commandParser.findCard(destinationPlayer, destinationRegion);
        assertEquals("197", destinationCard);
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
        String sourceCard = commandParser.findCard(sourcePlayer, sourceRegion);
        assertEquals("141", sourceCard);
        String destinationPlayer = commandParser.getPlayer(player);
        assertEquals(player, destinationPlayer);
        String destinationRegion = commandParser.getRegion(JolGame.READY_REGION);
        assertEquals(JolGame.READY_REGION, destinationRegion);
        String destinationCard = commandParser.findCard(destinationPlayer, destinationRegion);
        assertEquals("197", destinationCard);
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

    @Test(expected = CommandException.class)
    public void getPlayerNotSpecificEnough() throws CommandException {
        CommandParser commandParser = new CommandParser(new String[]{"move", "shan", "hand", "1", "ready", "1", "1"}, 1, game2);
        commandParser.getPlayer("ShanDow");
    }
}