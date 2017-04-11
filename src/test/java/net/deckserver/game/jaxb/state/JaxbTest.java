package net.deckserver.game.jaxb.state;

import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JaxbTest {

    @Test
    public void GameLoad() throws Exception {
        File gameFile = new File("src/test/resources/game1/game.xml");
        GameState gameState = FileUtils.loadGameState(gameFile);
        assertNotNull(gameState);
        assertEquals(5, gameState.getPlayer().size());
    }

    @Test
    public void ActionsLoad() throws Exception {
        File actionsFile = new File("src/test/resources/game1/actions.xml");
        GameActions gameActions = FileUtils.loadGameActions(actionsFile);
        assertNotNull(gameActions);
        assertEquals("3", gameActions.getCounter());
    }
}
