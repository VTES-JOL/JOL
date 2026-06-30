package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.persistence.EntityManager;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jobs.GameDataConversion;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameChatRepository;
import net.deckserver.jpa.repository.GameInfoRepository;
import net.deckserver.jpa.repository.GameStateRepository;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import static net.deckserver.JolAdmin.saveGameState;

public class GameService extends PersistedService {

    public static final Predicate<GameInfo> STARTING_GAME = (info) -> info.getStatus().equals(GameStatus.STARTING);
    public static final Predicate<GameInfo> PUBLIC_GAME = info -> info.getVisibility().equals(Visibility.PUBLIC);
    public static final Predicate<GameInfo> ACTIVE_GAME = (info) -> info.getStatus().equals(GameStatus.ACTIVE);
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final GameInfoRepository gameInfoRepository = new GameInfoRepository();
    private static final GameStateRepository gameStateRepository = new GameStateRepository();
    private static final GameChatRepository gameChatRepository = new GameChatRepository();
    private static final GameService INSTANCE = new GameService();
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Lock readLock = rwLock.readLock();
    private static final Lock writeLock = rwLock.writeLock();
    private final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(GameService::loadGame);
    private final Map<String, GameInfo> games = new HashMap<>();
    private final ConcurrentHashMap<String, String> idToName = new ConcurrentHashMap<>();
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

    public static String getNameByGameId(String gameId) {
        String name = INSTANCE.idToName.get(gameId);
        if (name == null) throw new IllegalArgumentException("No game with id: " + gameId);
        return name;
    }

    public static void create(String gameName, String gameId, String ownerName, Visibility visibility, GameFormat format) {
        if (gameName == null || gameName.isEmpty()) {
            logger.error("Game name is null or empty");
            return;
        }
        GameInfo gameInfo = new GameInfo(gameName, gameId, ownerName, visibility, GameStatus.STARTING, format);
        INSTANCE.games.put(gameName, gameInfo);
        INSTANCE.idToName.put(gameId, gameName);
        // game directory is still needed for turn snapshots (saveGame with turn param)
        try {
            Path gamePath = DataPaths.path("games", gameId);
            Files.createDirectory(gamePath);
        } catch (IOException e) {
            logger.error("Error creating game directory", e);
        }
        INSTANCE.jpaWrite(em -> gameInfoRepository.save(em, gameInfo));
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
                .map(GameInfo::getName)
                .sorted().toList();
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
        Path gamePath = DataPaths.path("games", gameId);
        INSTANCE.games.remove(gameName);
        INSTANCE.idToName.remove(gameId);
        try {
            FileUtils.deleteDirectory(gamePath.toFile());
        } catch (IOException e) {
            logger.error("Unable to delete game directory", e);
        }
        INSTANCE.jpaWrite(em -> {
            gameChatRepository.delete(em, gameId);
            gameStateRepository.delete(em, gameId);
            gameInfoRepository.delete(em, gameName);
        });
    }

    public static JolGame loadGame(String gameId) {
        readLock.lock();
        try {
            if (INSTANCE.testModeEnabled) {
                // Load test fixture from file
                Path gameStatePath = DataPaths.path("games", gameId, "game.json");
                if (gameStatePath.toFile().exists()) {
                    try {
                        GameData gameData = objectMapper.readValue(gameStatePath.toFile(), GameData.class);
                        return new JolGame(gameId, gameData);
                    } catch (IOException e) {
                        logger.error("Error reading game fixture {}", gameId, e);
                    }
                }
                return new JolGame(gameId, new GameData(gameId));
            }
            try (EntityManager em = JpaFactory.createEntityManager()) {
                GameData gameData = gameStateRepository.load(em, gameId);
                if (gameData != null) {
                    return new JolGame(gameId, gameData);
                }
            } catch (Exception e) {
                logger.error("JPA load failed for game {}", gameId, e);
            }
        } finally {
            readLock.unlock();
        }
        return new JolGame(gameId, new GameData(gameId));
    }

    public static JolGame loadSnapshot(String gameId, String turn) {
        try {
            Path gameStatePath = DataPaths.path("games", gameId, "game-" + turn + ".json");
            GameData gameData = objectMapper.readValue(gameStatePath.toFile(), GameData.class);
            return new JolGame(gameId, gameData);
        } catch (IOException e) {
            logger.error("Error reading game snapshot file", e);
        }
        return new JolGame(gameId, new GameData(gameId));
    }

    public static void rollbackGame(String gameName, String turn) {
        String id = get(gameName).getId();
        JolGame game = loadSnapshot(id, turn);
        saveGameState(game, true);
        INSTANCE.gameCache.refresh(gameName);
    }

    public static void saveGame(JolGame game) {
        writeLock.lock();
        try {
            INSTANCE.gameCache.put(game.id(), game);
        } finally {
            writeLock.unlock();
        }
        String gameName = INSTANCE.idToName.get(game.id());
        if (gameName != null && INSTANCE.games.containsKey(gameName)) {
            INSTANCE.jpaWrite(em -> gameStateRepository.save(em, game));
        }
    }

    public static void saveGame(JolGame game, String turn) {
        // File snapshot for rollback — intentionally file-based
        if (INSTANCE.testModeEnabled) return;
        turn = turn.replaceAll("\\.", "-");
        String gameId = game.id();
        Path gameStatePath = DataPaths.path("games", gameId, "game-" + turn + ".json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(gameStatePath.toFile(), game.data());
        } catch (IOException e) {
            logger.error("Unable to save game snapshot", e);
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
        String activePlayer = game.getActivePlayer();
        PlayerSummary activePlayerSummary = new PlayerSummary();
        activePlayerSummary.setName(activePlayer);
        activePlayerSummary.setPool(game.getPool(activePlayer));
        summary.setActivePlayer(activePlayerSummary);
        String predator = game.getPredatorOf(activePlayer);
        if (predator != null) {
            PlayerSummary predatorSummary = new PlayerSummary();
            predatorSummary.setName(predator);
            predatorSummary.setPool(game.getPool(predator));
            summary.setPredator(predatorSummary);
        }
        String prey = game.getPreyOf(activePlayer);
        if (prey != null) {
            PlayerSummary preySummary = new PlayerSummary();
            preySummary.setName(prey);
            preySummary.setPool(game.getPool(prey));
            summary.setPrey(preySummary);
        }
        return summary;
    }

    public static JolGame getGameByName(String gameName) {
        GameInfo gameInfo = get(gameName);
        return INSTANCE.gameCache.get(gameInfo.getId());
    }

    private void upgrade() {
        logger.info("Determining upgrades...");
        GameDataConversion conversion = new GameDataConversion();
        games.values().stream()
                .filter(ACTIVE_GAME)
                .filter(Objects::nonNull)
                .filter(gameInfo -> gameInfo.getVersion().isOlderThan(GameInfo.Version.GAME_STATE))
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

        jpaWrite(em -> games.values().forEach(g -> gameInfoRepository.save(em, g)));
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }
        // Flush cached game states to JPA
        gameCache.asMap().values().forEach(GameService::saveGame);
        logger.debug("Persisted {} games in cache", gameCache.estimatedSize());
        // Sync any in-memory GameInfo mutations to JPA
        jpaWrite(em -> games.values().forEach(g -> gameInfoRepository.save(em, g)));
    }

    @Override
    protected void load() {
        if (testModeEnabled) {
            Path path = DataPaths.path("games.json");
            if (!Files.exists(path)) return;
            try {
                Map<String, GameInfo> loaded = objectMapper.readValue(path.toFile(), new TypeReference<>() {});
                games.putAll(loaded);
                games.forEach((name, info) -> idToName.put(info.getId(), name));
                logger.info("Loaded {} games from file", games.size());
            } catch (IOException e) {
                logger.error("Unable to load games from file", e);
            }
            return;
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            Map<String, GameInfo> loaded = gameInfoRepository.findAll(em);
            games.putAll(loaded);
            games.forEach((name, info) -> idToName.put(info.getId(), name));
            logger.info("Loaded {} games from JPA", games.size());
        } catch (Exception e) {
            logger.error("JPA load failed for GameService", e);
        }
    }

    private void jpaWrite(java.util.function.Consumer<EntityManager> action) {
        if (testModeEnabled) return;
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for GameService", e);
        }
    }
}
