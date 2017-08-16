package deckserver.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PromoteAdmin {

    private static final Logger logger = LoggerFactory.getLogger(PromoteAdmin.class);
    private static final String sourceDirectory = "/Users/shannon/data";
    private static Path dataPath = Paths.get(sourceDirectory);

    private static Properties load(Path propertyPath) {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader(propertyPath.toFile())) {
            properties.load(fileReader);
            return properties;
        } catch (FileNotFoundException e) {
            logger.error("Unable to find file {}", propertyPath);
        } catch (IOException e) {
            logger.error("Error reading property file {}", propertyPath);
        }
        return null;
    }

    private static void save(Properties properties, Path propertyPath) {
        try (FileWriter fileWriter = new FileWriter(propertyPath.toFile())) {
            properties.store(fileWriter, "Deckserver 3.0 player file");
        } catch (IOException e) {
            logger.error("Error writing property file {}", propertyPath);
        }
    }

    public static void main(String[] args) {
        Properties systemProperties = load(dataPath.resolve("system.properties"));
        systemProperties.stringPropertyNames().stream().filter(s -> s.matches("^player\\d*")).forEach(PromoteAdmin::promotePlayer);
    }

    private static void promotePlayer(String playerId) {
        Path playerPath = dataPath.resolve(playerId).resolve("player.properties");
        Properties playerProperties = load(playerPath);
        playerProperties.setProperty("admin", "yes");
        save(playerProperties, playerPath);
    }
}
