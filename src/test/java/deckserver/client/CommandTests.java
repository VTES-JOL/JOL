package deckserver.client;

import deckserver.dwr.GameModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shannon on 23/11/16.
 */
public class CommandTests {

    private static String SPLIT_PATTERN = ";";

    private GameModel game;

    @Before
    public void setUp() throws Exception {
        JolAdmin.getInstance();
        game = new GameModel("Game1");
        assertNotNull(game);
    }

    @Test
    public void commandSplitting() throws Exception {
        String singleCommand = "blood ready 1 +1";
        String[] strings = singleCommand.split(SPLIT_PATTERN);
        assertEquals(1, strings.length);

        String singleCommandEndingColon = "blood ready 1 +1;";
        strings = singleCommandEndingColon.split(SPLIT_PATTERN);
        assertEquals(1, strings.length);

        String multipleCommandsNoSpace = "untap;blood ready 1 +1";
        strings = multipleCommandsNoSpace.split(SPLIT_PATTERN);
        assertEquals(2, strings.length);

        String multipleCommandsWithSpaces = "untap; blood ready 1 +1";
        strings = multipleCommandsWithSpaces.split(SPLIT_PATTERN);
        assertEquals(2, strings.length);

        String multipleCommandsWithLeadingSpaces = "untap ; blood ready 1 +1";
        strings = multipleCommandsWithLeadingSpaces.split(SPLIT_PATTERN);
        assertEquals(2, strings.length);
    }

    @Test
    public void chainedCommands() throws Exception {
        String result = game.submit("Player3", "UNTAP", "untap", null, null, null, null, null);
        assertNotNull(result);
        System.out.println(result);
    }
}
