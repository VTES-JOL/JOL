package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jobs.GameDataConversion;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.GameSummary;
import net.deckserver.storage.json.game.PlayerSummary;
import net.deckserver.storage.json.system.GameInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class GameService extends PersistedService {

    public static final Predicate<GameInfo> STARTING_GAME = (info) -> info.getStatus().equals(GameStatus.STARTING);
    public static final Predicate<GameInfo> PUBLIC_GAME = info -> info.getVisibility().equals(Visibility.PUBLIC);
    public static final Predicate<GameInfo> ACTIVE_GAME = (info) -> info.getStatus().equals(GameStatus.ACTIVE);
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "games.json");
    private static final GameService INSTANCE = new GameService();
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Lock readLock = rwLock.readLock();
    private static final Lock writeLock = rwLock.writeLock();
    private final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(GameService::loadGame);
    private final Map<String, GameInfo> games = new HashMap<>();
    private final LoadingCache<String, GameSummary> summaryMap = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .refreshAfterWrite(30, TimeUnit.SECONDS)
            .build(GameService::generateSummary);

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

    public static List<String> getActiveGames() {
        return INSTANCE.games.values().stream().filter(ACTIVE_GAME).map(GameInfo::getName).sorted().toList();
    }

    public static long getPublicGameCount(GameFormat format) {
        return INSTANCE.games.values().stream()
                .filter(STARTING_GAME)
                .filter(PUBLIC_GAME)
                .filter(info -> info.getGameFormat().equals(format))
                .count();
    }

    public static List<String> getStartingGames(boolean includePlayTest) {
        return INSTANCE.games.values().stream()
                .filter(STARTING_GAME)
                .filter(info -> info.isPlayTest() && includePlayTest)
                .map(GameInfo::getName
                ).sorted().toList();
    }

    public static List<GameInfo> getGamesByOwner(String owner) {
        return INSTANCE.games.values().stream().filter(info -> info.getOwner().equals(owner)).toList();
    }

    public static List<String> getActiveGames(String owner) {
        return INSTANCE.games.values().stream()
                .filter(ACTIVE_GAME)
                .filter(info -> info.getOwner().equals(owner))
                .map(GameInfo::getName)
                .toList();
    }

    public static boolean isActive(String gameName) {
        return get(gameName).getStatus().equals(GameStatus.ACTIVE);
    }

    public static boolean isStarting(String gameName) {
        return get(gameName).getStatus().equals(GameStatus.STARTING);
    }

    public static boolean isPublic(String gameName) {
        return get(gameName).getVisibility().equals(Visibility.PUBLIC);
    }

    public static boolean isPrivate(String gameName) {
        return get(gameName).getVisibility().equals(Visibility.PRIVATE);
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

    public static JolGame loadGame(String gameId) {
        readLock.lock();
        try {
            Path gameStatePath = Paths.get(System.getenv("JOL_DATA"), "games", gameId, "game.json");
            GameData gameData = objectMapper.readValue(gameStatePath.toFile(), GameData.class);
            return new JolGame(gameId, gameData);
        } catch (IOException e) {
            logger.error("Error reading game file {}", gameId, e);
        } finally {
            readLock.unlock();
        }
        return new JolGame(gameId, new GameData(gameId));
    }

    public static JolGame loadSnapshot(String gameId, String turn) {
        try {
            Path gameStatePath = Paths.get(System.getenv("JOL_DATA"), "games", gameId, "game-" + turn + ".json");
            GameData gameData = objectMapper.readValue(gameStatePath.toFile(), GameData.class);
            return new JolGame(gameId, gameData);
        } catch (IOException e) {
            logger.error("Error reading game file", e);
        }
        return new JolGame(gameId, new GameData(gameId));
    }

    public static void saveGame(JolGame game) {
        writeLock.lock();
        String gameId = game.id();
        Path gameStatePath = Paths.get(System.getenv("JOL_DATA"), "games", gameId, "game.json");
        GameData deckServerState = game.data();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(gameStatePath.toFile(), deckServerState);
        } catch (IOException e) {
            logger.error("Unable to save game file", e);
        } finally {
            writeLock.unlock();
        }
        // update the cache
        INSTANCE.gameCache.put(gameId, game);
    }

    public static void saveGame(JolGame game, String turn) {
        boolean testModeEnabled = System.getenv().getOrDefault("ENABLE_TEST_MODE", "false").equals("true");
        if (testModeEnabled) {
            return;
        }
        turn = turn.replaceAll("\\.", "-");
        String gameId = game.id();
        Path gameStatePath = Paths.get(System.getenv("JOL_DATA"), "games", gameId, "game-" + turn + ".json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(gameStatePath.toFile(), game.data());
        } catch (IOException e) {
            logger.error("Unable to save game file", e);
        }
    }

    public static JolGame getGame(String gameId) {
        return INSTANCE.gameCache.get(gameId);
    }

    public static GameSummary getSummary(String gameName) {
        return INSTANCE.summaryMap.get(gameName);
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    private static GameSummary generateSummary(String gameName) {
        logger.debug("Regenerating summary for {}", gameName);
        GameInfo info = INSTANCE.games.get(gameName);
        JolGame game = INSTANCE.gameCache.get(info.getId());
        GameSummary summary = new GameSummary();
        summary.setName(game.getName());
        summary.setId(game.id());
        summary.setPhase(game.getPhase().toString());
        summary.setTurnLabel(game.getTurnLabel());
        summary.setPlayers(game.getValidPlayers());
        summary.setFormat(info.getGameFormat());
        // build active player summary
        String activePlayer = game.getActivePlayer();
        PlayerSummary activePlayerSummary = new PlayerSummary();
        activePlayerSummary.setName(activePlayer);
        activePlayerSummary.setPool(game.getPool(activePlayer));
        summary.setActivePlayer(activePlayerSummary);
        // Build predator summary
        String predator = game.getPredatorOf(activePlayer);
        if (predator != null) {
            PlayerSummary predatorSummary = new PlayerSummary();
            predatorSummary.setName(predator);
            predatorSummary.setPool(game.getPool(predator));
            summary.setPredator(predatorSummary);
        }
        // Build prey summary
        String prey = game.getPreyOf(activePlayer);
        if (prey != null) {
            PlayerSummary preySummary = new PlayerSummary();
            preySummary.setName(prey);
            preySummary.setPool(game.getPool(prey));
            summary.setPrey(preySummary);
        }
        return summary;
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

        games.values().stream()
                .filter(ACTIVE_GAME)
                .filter(Objects::nonNull)
                .filter(gameInfo -> gameInfo.getVersion().isOlderThan(GameInfo.Version.DATA_FIX))
                .peek(gameInfo -> logger.info("Validating data {} - {}", gameInfo.getName(), gameInfo.getId()))
                .forEach(gameInfo -> {
                    conversion.checkCards(gameInfo.getName(), gameInfo.getId());
                    gameInfo.setVersion(GameInfo.Version.DATA_FIX);
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
