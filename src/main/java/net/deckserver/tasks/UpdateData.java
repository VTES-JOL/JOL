package net.deckserver.tasks;

import net.deckserver.game.jaxb.FileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameCard;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.jaxb.state.Region;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UpdateData {

    private static final Predicate<String> PLAYER_PREDICATE = s -> s.matches("^player\\d{1,4}");
    private static final Predicate<String> GAME_PREDICATE = s -> s.matches("^game\\d{1,4}");

    public static void main(String[] args) throws Exception {

        Path basePath = Paths.get(args[0]);
        if (!basePath.toFile().exists()) {
            System.err.println("Path does not exist");
            System.exit(1);
        }

        List<String> players = getPlayerList(basePath);
        System.out.println("Found " + players.size() + " players");

        List<String> games = getGameList(basePath);
        System.out.println("Found " + games.size() + " games");

        closeOldGames(basePath);
        updatePlayerName(basePath, "KRC 1+1+1, +1 on me", "Fredrik");
    }

    private static void closeOldGames(Path basePath){
        try {
            List<String> closedGames = new ArrayList<>();
            for (String gameId : getGameList(basePath)) {
                Properties gameProperties = loadGameFile(basePath, gameId);
                if (gameProperties.getProperty("state", "finished").equals("finished")) {
                    closedGames.add(gameId);
                    deleteDirectory(basePath.resolve(gameId).toFile());
                }
            }
            System.out.println("Found " + closedGames.size() + " closed games");
            List<String> players = getPlayerList(basePath);
            for (String playerId : players) {
                Properties playerProperties = loadPlayerFile(basePath, playerId);
                closedGames.forEach(playerProperties::remove);
                savePlayerFile(playerProperties, playerId, basePath);
            }
            Properties systemProperties = loadSystemFile(basePath);
            closedGames.forEach(systemProperties::remove);
            saveSystemFile(systemProperties, basePath);
        } catch (IOException e) {
            System.err.println("Error closing games");
            e.printStackTrace(System.err);
        }
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    private static void updatePlayerName(Path basePath, String oldName, String newName) {
        try {
            String playerId = getId(basePath, oldName);
            // Update the global playerList
            Properties systemProperties = loadSystemFile(basePath);
            //systemProperties.setProperty(playerId, newName);
            System.out.println("Updated " + oldName + " in global database to " + newName);
            saveSystemFile(systemProperties, basePath);
            // Update the player database
            Properties playerProperties = loadPlayerFile(basePath, playerId);
            //playerProperties.setProperty("name", newName);
            System.out.println("Updated " + oldName + " in player " + playerId + " database to " + newName);
            savePlayerFile(playerProperties, playerId, basePath);
            // Find all the games the player is in
            List<String> games = getGameList(basePath);
            System.out.println("Loaded " + games.size() + " games");
            for (String gameId : games) {
                List<String> players = getPlayerList(basePath, gameId);
                if (players.contains(playerId)) {
                    // Update the game state
                    File gameFile = basePath.resolve(gameId).resolve("game.xml").toFile();
                    GameState gameState = FileUtils.loadGameState(gameFile);
                    for (Region region : gameState.getRegion()) {
                        if (region.getName().startsWith(oldName)) {
                            String newRegionName = region.getName().replace(oldName, newName);
                            region.setName(newRegionName);
                        }
                        for (GameCard card : region.getGameCard()) {
                            if (card.getOwner().equals(oldName)) {
                                card.setOwner(newName);
                            }
                        }
                    }
                    for (Notation notation : gameState.getNotation()) {
                        if (notation.getName().startsWith(oldName)) {
                            String newNotationName = notation.getName().replace(oldName, newName);
                            notation.setName(newNotationName);
                        }
                        if (notation.getValue() != null && notation.getValue().equals(oldName)) {
                            notation.setValue(newName);
                        }
                    }
                    int playerIndex = gameState.getPlayer().indexOf(oldName);
                    gameState.getPlayer().set(playerIndex, newName);
                    System.out.println("Updating " + gameId + " to replace player " + oldName + " with " + newName);
                    FileUtils.saveGameState(gameState, gameFile);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String getId(Path basePath, String name) throws IOException {
        Properties systemProperties = loadSystemFile(basePath);
        for (Map.Entry<Object, Object> objectObjectEntry : systemProperties.entrySet()) {
            if (objectObjectEntry.getValue().equals(name)) {
                return (String) ((Map.Entry<?, ?>) objectObjectEntry).getKey();
            }
        }
        throw new RuntimeException("Unable to find id with that name: " + name);
    }

    private static List<String> getPlayerList(Path basePath) throws IOException {
        return loadSystemFile(basePath).stringPropertyNames().stream()
                .filter(PLAYER_PREDICATE)
                .collect(Collectors.toList());
    }

    private static List<String> getPlayerList(Path basePath, String gameId) throws IOException {
        return loadGameFile(basePath, gameId).stringPropertyNames().stream()
                .filter(PLAYER_PREDICATE)
                .collect(Collectors.toList());
    }

    private static List<String> getGameList(Path basePath) throws IOException {
        return loadSystemFile(basePath).stringPropertyNames().stream()
                .filter(GAME_PREDICATE)
                .collect(Collectors.toList());
    }

    private static Properties loadSystemFile(Path basePath) throws IOException {
        return loadProperties(basePath.resolve("system.properties"));
    }

    private static Properties loadPlayerFile(Path basePath, String playerId) throws IOException {
        return loadProperties(basePath.resolve(playerId).resolve("player.properties"));
    }

    private static Properties loadGameFile(Path basePath, String gameId) throws IOException {
        return loadProperties(basePath.resolve(gameId).resolve("game.properties"));
    }

    private static void saveSystemFile(Properties properties, Path basePath) throws IOException {
        saveProperties(properties, basePath.resolve("system.properties"), "Deckserver 3.0 System file");
    }

    private static void savePlayerFile(Properties properties, String playerId, Path basePath) throws IOException {
        saveProperties(properties, basePath.resolve(playerId).resolve("player.properties"), "Deckserver 3.0 Player file");
    }

    private static void saveGameFile(Properties properties, String gameId, Path basePath) throws IOException {
        saveProperties(properties, basePath.resolve(gameId).resolve("game.properties"), "game.properties");
    }

    private static void saveProperties(Properties properties, Path path, String message) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            properties.store(fileWriter, message);
        }
    }

    private static Properties loadProperties(Path path) throws IOException {
        Properties properties = new Properties();
        File propertiesFile = path.toFile();
        if (!propertiesFile.exists()) {
            System.out.println("Unable to find properties file at location " + path);
            System.exit(1);
        }
        try (FileReader systemReader = new FileReader(propertiesFile)) {
            properties.load(systemReader);
            return properties;
        }
    }
}
