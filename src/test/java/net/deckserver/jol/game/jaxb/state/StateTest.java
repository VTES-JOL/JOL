package net.deckserver.jol.game.jaxb.state;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;

public class StateTest {

    @Test
    public void GameLoad() throws Exception {
        JAXBContext context = JAXBContext.newInstance("net.deckserver.jol.game.jaxb.state");
        Assert.assertNotNull(context);
        Source source = new StreamSource(new FileInputStream("src/test/resources/game1/game.xml"));
        Assert.assertNotNull(source);
        JAXBElement<GameState> gameState = context.createUnmarshaller().unmarshal(source, GameState.class);
        Assert.assertNotNull(gameState);
    }
}
