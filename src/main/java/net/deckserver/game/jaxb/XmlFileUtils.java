package net.deckserver.game.jaxb;

import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class XmlFileUtils {

    private final static Logger logger = LoggerFactory.getLogger(XmlFileUtils.class);

    private final static String GAME_STATE_CONTEXT = "net.deckserver.game.jaxb.state";
    private final static String GAME_ACTIONS_CONTEXT = "net.deckserver.game.jaxb.actions";

    public static GameState loadGameState(Path path) {
        return loadFromFile(GAME_STATE_CONTEXT, GameState.class, path.toFile());
    }

    public static GameActions loadGameActions(Path path) {
        return loadFromFile(GAME_ACTIONS_CONTEXT, GameActions.class, path.toFile());
    }

    private static <T> T loadFromFile(String contextPath, Class<T> type, File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Source source = new StreamSource(fileInputStream);
            JAXBElement<T> element = context.createUnmarshaller().unmarshal(source, type);
            return element.getValue();
        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Unable to load " + file.getName(), e);
        }
    }

    public static void saveGameActions(GameActions gameActions, Path path) {
        saveToFile(GAME_ACTIONS_CONTEXT, GameActions.class, gameActions, path.toFile());
    }

    public static void saveGameState(GameState gameState, Path path) {
        saveToFile(GAME_STATE_CONTEXT, GameState.class, gameState, path.toFile());
    }

    private static <T> void saveToFile(String contextPath, Class<T> type, T data, File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            context.createMarshaller().marshal(data, file);
        } catch (JAXBException e) {
            logger.error("Unable to save to file",e);
            throw new RuntimeException("Unable to create JAXB context for " + type.getName(), e);
        }
    }
}
