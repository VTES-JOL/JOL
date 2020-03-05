package net.deckserver.game.jaxb.state;

import net.deckserver.game.jaxb.FileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@Ignore
public class JaxbTest {

    @Test
    public void GameLoad() throws Exception {
        File gameFile = new File("src/test/resources/data/game1/game.xml");
        GameState gameState = FileUtils.loadGameState(gameFile);
        assertNotNull(gameState);
        List<String> players = gameState.getPlayer();
        assertThat(players.size(), is(5));
        assertThat(players.get(0), is("Player4"));
        assertThat(players.get(1), is("Player5"));
        assertThat(players.get(2), is("Player2"));
        assertThat(players.get(3), is("Player3"));
        assertThat(players.get(4), is("Player1"));
    }

    @Test
    public void ActionsLoad() throws Exception {
        File actionsFile = new File("src/test/resources/data/game1/actions.xml");
        GameActions gameActions = FileUtils.loadGameActions(actionsFile);
        assertNotNull(gameActions);
        assertEquals("3", gameActions.getCounter());
    }
}
