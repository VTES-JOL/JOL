package net.deckserver.game.jaxb.state;

import net.deckserver.game.jaxb.XmlFileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore
public class JaxbTest {

    @Test
    public void GameLoad() throws Exception {
        Path gamePath = Paths.get("src/test/resources/data/game1/game.xml");
        GameState gameState = XmlFileUtils.loadGameState(gamePath);
        assertThat(gameState, not(null));
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
        Path actionsPath = Paths.get("src/test/resources/data/game1/actions.xml");
        GameActions gameActions = XmlFileUtils.loadGameActions(actionsPath);
        assertThat(gameActions, not(null));
        assertThat(gameActions.getCounter(), is("3"));
    }
}
