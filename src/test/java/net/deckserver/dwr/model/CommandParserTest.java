package net.deckserver.dwr.model;

import net.deckserver.game.storage.state.RegionType;
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

    @Before
    public void setUp() {
        environmentVariables.set("JOL_DATA", "src/test/resources/data");
        game = ModelLoader.loadGame("01JHQ7QXHB7SR86F3RNVSXFVMN");
    }

    @Test
    public void testCommands() throws Exception {
        assertNotNull(game);
        DoCommand command = new DoCommand(game);
        assertEquals(4, game.getSize("Player5", RegionType.UNCONTROLLED));
        assertEquals(0, game.getSize("Player5", RegionType.READY));
        command.doCommand("Player5", new String[]{"move", "inactive", "1", "ready"});
        assertEquals(3, game.getSize("Player5", RegionType.UNCONTROLLED));
        assertEquals(1, game.getSize("Player5", RegionType.READY));
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

}