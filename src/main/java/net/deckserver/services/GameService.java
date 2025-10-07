package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jobs.GameDataConversion;
import net.deckserver.storage.json.system.GameInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class GameService extends PersistedService {

    public static final Predicate<GameInfo> STARTING_GAME = (info) -> info.getStatus().equals(GameStatus.STARTING);
    public static final Predicate<GameInfo> PUBLIC_GAME = info -> info.getVisibility().equals(Visibility.PUBLIC);
    public static final Predicate<GameInfo> ACTIVE_GAME = (info) -> info.getStatus().equals(GameStatus.ACTIVE);
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "games.json");
    private static final GameService INSTANCE = new GameService();

    private final Map<String, GameInfo> games = new HashMap<>();

    private GameService() {
        super("GameService", 5);
        load();
        upgrade();
    }

    public static GameInfo get(String name) {
        return INSTANCE.games.get(name);
    }

    public static void create(String gameName, String gameId, String ownerName, Visibility visibility, GameFormat format) {
        GameInfo gameInfo = new GameInfo(gameName, gameId, ownerName, visibility, GameStatus.STARTING, format);
        INSTANCE.games.put(gameName, gameInfo);
        try {
            Path gamePath = Paths.get(System.getenv("JOL_DATA"), "games", gameId);
            Files.createDirectory(gamePath);
        } catch (IOException e) {
            logger.error("Error creating game directory", e);
        }
    }

    public static boolean existsGame(String name) {
        return INSTANCE.games.containsKey(name);
    }

    public static Set<String> getGameNames() {
        return INSTANCE.games.keySet();
    }

    public static void remove(String gameName, String gameId) {
        Path gamePath = Path.of(System.getenv("JOL_DATA"), "games", gameId);
        INSTANCE.games.remove(gameName);
        try {
            FileUtils.deleteDirectory(gamePath.toFile());
        } catch (IOException e) {
            logger.error("Unable to delete game directory", e);
        }
    }

    private static GameInfo loadGameInfo(String gameName) {
        if (INSTANCE.games.containsKey(gameName)) {
            return INSTANCE.games.get(gameName);
        }
        throw new IllegalArgumentException("Game: " + gameName + " was not found.");
    }

    private void upgrade() {
        logger.info("Determining upgrades...");
        GameDataConversion conversion = new GameDataConversion();
        // Upgrade all games with no version
        games.values().stream()
                .filter(ACTIVE_GAME)
                .filter(Objects::nonNull)
                // Version 1 is the first version where we store additional information in the game state
                .filter(gameInfo -> gameInfo.getVersion().isOlderThan(GameInfo.Version.GAME_STATE))
                // For every active game check the game state
                .peek(gameInfo -> logger.info("Upgrading game {} - {}", gameInfo.getName(), gameInfo.getId()))
                .forEach(gameInfo -> {
                    conversion.convertGame(gameInfo.getId());
                    gameInfo.setVersion(GameInfo.Version.GAME_STATE);
                });
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} game data", games.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), games);
            logger.debug("Successfully persisted game data");
        } catch (IOException e) {
            logger.error("Unable to save game data", e);
        }
    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing games file found");
            return;
        }

        try {
            Map<String, GameInfo> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {
            });
            games.putAll(loaded);
            logger.info("Loaded {} games", games.size());
        } catch (IOException e) {
            logger.error("Unable to load games.", e);
        }
    }
}
