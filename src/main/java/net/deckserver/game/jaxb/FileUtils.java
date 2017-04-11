package net.deckserver.game.jaxb;

import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileUtils {

    private final static String GAME_STATE_CONTEXT = "net.deckserver.game.jaxb.state";
    private final static String GAME_ACTIONS_CONTEXT = "net.deckserver.game.jaxb.actions";

    public static GameState loadGameState(File file) {
        return loadFromFile(GAME_STATE_CONTEXT, GameState.class, file);
    }

    public static GameActions loadGameActions(File file) {
        return loadFromFile(GAME_ACTIONS_CONTEXT, GameActions.class, file);
    }

    private static <T> T loadFromFile(String contextPath, Class<T> type, File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Source source = new StreamSource(new FileInputStream(file));
            JAXBElement<T> element = context.createUnmarshaller().unmarshal(source, type);
            return element.getValue();
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to create JAXB context for " + type.getName(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(type.getName() + " file not found", e);
        }
    }

    public static void saveGameActions(GameActions gameActions, File file) {
        saveToFile(GAME_ACTIONS_CONTEXT, GameActions.class, gameActions, file);
    }

    public static void saveGameState(GameState gameState, File file) {
        saveToFile(GAME_STATE_CONTEXT, GameState.class, gameState, file);
    }

    private static <T> void saveToFile(String contextPath, Class<T> type, T data, File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            context.createMarshaller().marshal(data, file);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to create JAXB context for " + type.getName(), e);
        }
    }
}
