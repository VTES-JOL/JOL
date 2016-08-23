package net.deckserver.jol.game;

import net.deckserver.jol.game.jaxb.state.GameState;
import net.deckserver.jol.game.jaxb.turn.GameActions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by shannon on 24/07/2016.
 */
public class FileUtils {

    public static GameState loadGameState(File file) {
        return loadFromFile("net.deckserver.jol.game.state", GameState.class, file);
    }

    public static GameActions loadGameActions(File file) {
        return loadFromFile("net.deckserver.jol.game.turn", GameActions.class, file);
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
        saveToFile("net.deckserver.jol.game.turn", GameActions.class, gameActions, file);
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
