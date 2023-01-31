package net.deckserver.game.jaxb.state;

import net.deckserver.game.jaxb.XmlFileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Updater {

    private static final String BASE_PATH = "src/test/resources/data/";
    private static Set<String> allGames = new HashSet<>();
    private static Set<String> closedGames = new HashSet<>();
    private static Set<String> runningGames = new HashSet<>();
    private static Set<String> openGames = new HashSet<>();
    private static Set<String> activeGames = new HashSet<>();

    private static Set<String> playerNames = new HashSet<>();
    private static Properties idProperties = new Properties();
    private static Map<String, Set<String>> playerGames = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Updater.class);

    @Test
    public void updateGames() throws Exception {
        idProperties.load(new FileReader(new File("src/test/resources/data/cards/card-id.properties")));
        assertFalse(idProperties.isEmpty());
        Properties systemProperties = new Properties();
        File systemFile = new File(BASE_PATH, "system.properties");
        assertTrue(systemFile.exists());
        systemProperties.load(new FileReader(systemFile));

        allGames = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.matches("^game\\d{1,4}"))
                .collect(Collectors.toSet());
        assertFalse(allGames.isEmpty());

        playerNames = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.matches("^player\\d{1,4}"))
                .collect(Collectors.toSet());
        assertFalse(playerNames.isEmpty());

        closedGames = allGames.stream()
                .filter(this::gameIsClosed)
                .collect(Collectors.toSet());

        activeGames.addAll(allGames);
        activeGames.removeAll(closedGames);

        runningGames = allGames.stream()
                .filter(this::gameIsRunning)
                .collect(Collectors.toSet());

        openGames = allGames.stream()
                .filter(this::gameIsOpen)
                .collect(Collectors.toSet());

        LOGGER.info("Open Games: {}", openGames);
        LOGGER.info("Current Games: {}", runningGames);
        LOGGER.info("Closed Games: {}", closedGames);

        // Build a list of all games by player that are still active
        runningGames
                .forEach(gameId -> {
                    Set<String> gamePlayers = this.getGamePlayers(gameId);
                    gamePlayers.forEach(playerId -> {
                        playerGames.computeIfAbsent(playerId, d -> new HashSet<>()).add(gameId);
                    });
                });

        playerGames.keySet().forEach(playerId -> {
            Set<String> games = playerGames.get(playerId);
            games.removeAll(closedGames);
            assertFalse(closedGames.containsAll(games));
            assertTrue(activeGames.containsAll(games));
        });

        closedGames.forEach(systemProperties::remove);
        closedGames.forEach(this::deleteGame);
        playerNames.forEach(this::removeClosedGames);
        activeGames.forEach(this::updateCardIds);
        systemProperties.store(new FileWriter(systemFile), "DeckServer 3.0 system file");
    }

    private Set<String> getGamePlayers(String gameId) {
        return loadProperties(gameId, "game")
                .map(properties -> properties.stringPropertyNames().stream()
                        .filter(s -> s.matches("^player\\d{1,4}"))
                        .collect(Collectors.toSet())).orElse(new HashSet<>());
    }


    private void removeClosedGames(String playerId) {
        try {
            File propertiesFile = Paths.get(BASE_PATH, playerId, "player.properties").toFile();
            Properties playerProperties = new Properties();
            playerProperties.load(new FileReader(propertiesFile));
            Set<String> games = playerProperties.stringPropertyNames().stream().filter(s -> s.matches("^game\\d{1,4}")).collect(Collectors.toSet());
            Set<String> activeGames = playerGames.get(playerId);
            if (activeGames == null) {
                return;
            }
            games.removeAll(activeGames);
            games.forEach(playerProperties::remove);
            playerProperties.store(new FileWriter(propertiesFile), "Deckserver 3.0 player information");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String deleteGame(String gameId) {
        Path gamePath = Paths.get(BASE_PATH, gameId);
        deleteDirectory(gamePath.toFile());
        return gameId;
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private boolean gameIsOpen(String gameId) {
        return loadProperties(gameId, "game")
                .map(properties -> properties.getProperty("state", "closed").equals("open"))
                .orElse(false);
    }

    private boolean gameIsRunning(String gameId) {
        return loadProperties(gameId, "game")
                .map(properties -> properties.getProperty("state", "closed").equals("closed"))
                .orElse(false);
    }

    private boolean gameIsClosed(String gameId) {
        return loadProperties(gameId, "game")
                .map(properties -> properties.getProperty("state", "finished").equals("finished"))
                .orElse(true);
    }

    private Optional<Properties> loadProperties(String id, String prefix) {
        File propertiesFile = Paths.get(BASE_PATH, id, prefix + ".properties").toFile();
        try {
            Properties gameProperties = new Properties();
            gameProperties.load(new FileReader(propertiesFile));
            return Optional.of(gameProperties);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void updateCardIds(String gameId) {
        Path gameStatePath = Paths.get(BASE_PATH, gameId, "game.xml");
        if (Files.exists(gameStatePath)) {
            GameState gameState = XmlFileUtils.loadGameState(gameStatePath);

            LOGGER.info("Converting game state: {}", gameId);
            if (gameState.region != null && !gameState.region.isEmpty()) {
                gameState.region.stream()
                        .flatMap(region -> region.getGameCard().stream())
                        .filter(Objects::nonNull)
                        .forEach(gameCard -> {
                            String oldId = gameCard.getCardid();
                            String newId = idProperties.getProperty(oldId);
                            if (newId == null) {
                                return;
                            }
                            gameCard.setCardid(newId);
                            LOGGER.info("Converted card {} to {}", oldId, newId);
                        });

                XmlFileUtils.saveGameState(gameState, gameStatePath);
            }
        }

        // Update actions text
        Path gameActionsPath = Paths.get(BASE_PATH, gameId, "actions.xml");
        if (Files.exists(gameActionsPath)) {
            LOGGER.info("Updating cards in chat: {}", gameId);
            try {
                String action = new String(Files.readAllBytes(gameActionsPath));
                for (String oldId : idProperties.stringPropertyNames()) {
                    String newId = idProperties.getProperty(oldId);
                    if (newId != null) {
                        action = action.replaceAll(oldId, newId);
                    }
                }
                Files.write(gameActionsPath, action.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCardId(GameCard card) {
    }


}
