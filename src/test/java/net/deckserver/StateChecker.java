package net.deckserver;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StateChecker {

    private Path basePath;
    private Predicate<String> playerMatcher = (name) -> name.matches("^player\\d*");
    private Predicate<String> gameMatcher = (name) -> name.matches("^game\\d*");

    private Map<String, String> gameState = new HashMap<>();
    private Map<String, List<String>> startingGames = new HashMap<>();
    private Map<String, List<String>> currentGames = new HashMap<>();
    private Map<String, List<String>> finishedGames = new HashMap<>();

    public static void main(String[] args) throws IOException {
        StateChecker checker = new StateChecker("/home/shannon/data");
        checker.check();
    }

    private StateChecker(String dataDir) {
        basePath = Paths.get(dataDir);
    }

    private void check() throws IOException {
        checkGames();
        checkPlayers();
        report();
    }

    private void checkGames() {
        Properties systemProperties = load(basePath.resolve("system.properties"));
        systemProperties.stringPropertyNames()
                .stream()
                .filter(gameMatcher)
                .forEach(this::checkGame);
    }

    private void checkGame(String gameId) {
        Properties gameProperties = load(basePath.resolve(gameId).resolve("game.properties"));
        List<String> playerList = gameProperties.stringPropertyNames().stream().filter(playerMatcher).collect(Collectors.toList());
        String status = gameProperties.getProperty("state");
        gameState.put(gameId, status);
        switch (status) {
            case "closed":
                currentGames.put(gameId, playerList);
                break;
            case "open":
                startingGames.put(gameId, playerList);
                break;
            case "finished":
                finishedGames.put(gameId, playerList);
                System.out.println(gameId + " is finished");
                break;
        }
    }

    private void checkPlayers() throws IOException {
        Properties systemProperties = load(basePath.resolve("system.properties"));
        systemProperties.stringPropertyNames()
                .stream()
                .filter(playerMatcher)
                .forEach(this::checkPlayer);
    }

    private void checkPlayer(String playerId) {
        Properties playerProperties = load(basePath.resolve(playerId).resolve("player.properties"));
        List<String> gameList = playerProperties.stringPropertyNames().stream().filter(gameMatcher).collect(Collectors.toList());
        gameList.forEach(gameId -> {
            boolean invited = playerProperties.getProperty(gameId).equals("invited");
            boolean owner = playerProperties.getProperty(gameId).equals("owner");
            if (!invited && !owner) {
                String status = gameState.get(gameId);
                if (status == null) {
                    System.out.println(playerId + ": No game found : " + gameId);
                    return;
                }
                List<String> players = null;
                switch (status) {
                    case "open":
                        players = startingGames.get(gameId);
                        break;
                    case "closed":
                        players = currentGames.get(gameId);
                        break;
                    case "finished":
                        players = finishedGames.get(gameId);
                        break;
                }
                if (players == null || players.isEmpty()) {
                    System.out.println("Should be players for " + gameId);
                } else if (!players.contains(playerId)) {
                    System.out.println(gameId + ": No player found : " + playerId);
                }

            }
        });
    }

    private void report() {

    }

    private Properties load(Path propertyPath) {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader(propertyPath.toFile())) {
            properties.load(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
