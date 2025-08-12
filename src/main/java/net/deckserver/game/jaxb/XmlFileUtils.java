package net.deckserver.game.jaxb;

import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XmlFileUtils {

    private final static Logger logger = LoggerFactory.getLogger(XmlFileUtils.class);

    // Cache JAXBContexts per bound class
    private static final Map<Class<?>, JAXBContext> CONTEXTS = new ConcurrentHashMap<>();

    public static GameState loadGameState(Path path) {
        return loadFromFile(GameState.class, path.toFile());
    }

    public static GameActions loadGameActions(Path path) {
        return loadFromFile(GameActions.class, path.toFile());
    }

    private static <T> T loadFromFile(Class<T> type, File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            JAXBContext context = getContext(type);
            Source source = new StreamSource(fileInputStream);
            JAXBElement<T> element = context.createUnmarshaller().unmarshal(source, type);
            return element.getValue();
        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Unable to load " + file.getName(), e);
        }
    }

    public static void saveGameActions(GameActions gameActions, Path path) {
        saveToFile(GameActions.class, gameActions, path.toFile());
    }

    public static void saveGameState(GameState gameState, Path path) {
        saveToFile(GameState.class, gameState, path.toFile());
    }

    private static <T> void saveToFile(Class<T> type, T data, File file) {
        try {
            JAXBContext context = getContext(type);
            context.createMarshaller().marshal(data, file);
        } catch (JAXBException e) {
            logger.error("Unable to save to file",e);
            throw new RuntimeException("Unable to create JAXB context for " + type.getName(), e);
        }
    }

    private static <T> JAXBContext getContext(Class<T> type) throws JAXBException {
        return CONTEXTS.computeIfAbsent(type, key -> {
            try {
                // A) Prefer package-based context using our webapp classloader (bypasses TCCL)
                String pkg = key.getPackage().getName();
                ClassLoader appCl = XmlFileUtils.class.getClassLoader();
                return JAXBContext.newInstance(pkg, appCl);
            } catch (JAXBException first) {
                // B) Fallback: temporarily set TCCL for class-based context creation
                ClassLoader original = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(XmlFileUtils.class.getClassLoader());
                    return JAXBContext.newInstance(type);
                } catch (JAXBException second) {
                    // Attach useful hint and rethrow
                    String hint = "Ensure org.glassfish.jaxb:jaxb-runtime:4.x is packaged in WEB-INF/lib. " +
                            "If running in a container, avoid old javax JAXB libs in TOMCAT_HOME/lib. " +
                            "This code also forces the correct ClassLoader; if this still fails, " +
                            "verify that the JAXB-annotated classes and ObjectFactory are on the classpath.";
                    RuntimeException wrapped = new RuntimeException("Failed to create JAXBContext for " + type.getName() + ": " + second.getMessage() + " | " + hint, second);
                    throw wrapped;
                } finally {
                    Thread.currentThread().setContextClassLoader(original);
                }
            }
        });
    }

}
