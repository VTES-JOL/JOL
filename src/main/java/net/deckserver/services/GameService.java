package net.deckserver.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.persistence.EntityManager;
import net.deckserver.game.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.GameActivityRepository;
import net.deckserver.jpa.repository.GameChatRepository;
import net.deckserver.jpa.repository.GameInfoRepository;
import net.deckserver.jpa.repository.GameSnapshotRepository;
import net.deckserver.jpa.repository.GameStateRepository;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.GameSummary;
import net.deckserver.storage.json.game.PlayerSummary;
import net.deckserver.storage.json.system.GameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
    private static final GameSnapshotRepository gameSnapshotRepository = new GameSnapshotRepository();
    private static final GameActivityRepository gameActivityRepository = new GameActivityRepository();
    private static final GameService INSTANCE = new GameService();
    private final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(GameService::loadGame);
    private final Map<String, GameInfo> games = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> idToName = new ConcurrentHashMap<>();
    private final LoadingCache<String, GameSummary> summaryMap = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .refreshAfterWrite(30, TimeUnit.SECONDS)
            .build(GameService::generateSummary);

    private GameService() {
        super("GameService", 5);
        load();
    }

    public static GameInfo get(String name) {
        return INSTANCE.games.get(name);
    }

    /**
     * The only supported way to mutate a GameInfo already in the cache — writes through
     * to JPA and rolls the in-memory mutation back if that write fails, so a caller never
     * ends up with local state ahead of the database.
     */
    public static boolean updateGameInfo(String gameName, Consumer<GameInfo> mutation) {
        GameInfo gameInfo = INSTANCE.games.get(gameName);
        if (gameInfo == null) return false;
        GameInfo snapshot = copyGameInfo(gameInfo);
        return INSTANCE.jpaWriteWithRollback(
                () -> mutation.accept(gameInfo),
                em -> gameInfoRepository.save(em, gameInfo),
                () -> INSTANCE.games.put(gameName, snapshot));
    }

    private static GameInfo copyGameInfo(GameInfo source) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(source), GameInfo.class);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to copy game info " + source.getName(), e);
        }
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
        INSTANCE.jpaWriteThenMutate(
                em -> gameInfoRepository.save(em, gameInfo),
                () -> {
                    INSTANCE.games.put(gameName, gameInfo);
                    INSTANCE.idToName.put(gameId, gameName);
                });
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

    public static List<GameInfo> getGamesByTournament(String tournamentName) {
        return INSTANCE.games.values().stream().filter(info -> tournamentName.equals(info.getTournamentName())).toList();
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
        INSTANCE.jpaWriteThenMutate(
                em -> {
                    gameSnapshotRepository.deleteAllForGame(em, gameId);
                    gameChatRepository.delete(em, gameId);
                    gameStateRepository.delete(em, gameId);
                    // must run before gameInfoRepository.delete - looks up the game row by name first
                    gameActivityRepository.delete(em, gameName);
                    gameInfoRepository.delete(em, gameName);
                },
                () -> {
                    INSTANCE.games.remove(gameName);
                    INSTANCE.idToName.remove(gameId);
                    INSTANCE.gameCache.invalidate(gameId);
                    INSTANCE.summaryMap.invalidate(gameName);
                });
    }

    public static JolGame loadGame(String gameId) {
        // Load failures must propagate: falling back to an empty GameData here would
        // cache an empty game, and the scheduled write-through could then overwrite
        // the real DB row with it. Caffeine serializes loads per key.
        try (EntityManager em = JpaFactory.createEntityManager()) {
            GameData gameData = gameStateRepository.load(em, gameId);
            if (gameData != null) {
                return new JolGame(gameId, gameData);
            }
        }
        // no row — a game that has never been saved legitimately starts empty
        return new JolGame(gameId, new GameData(gameId));
    }

    public static JolGame loadSnapshot(String gameId, String turn) {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            GameData gameData = gameSnapshotRepository.load(em, gameId, snapshotTurn(turn));
            if (gameData == null) {
                // a missing snapshot must fail the rollback, not roll back to an empty game
                throw new IllegalStateException("No snapshot for game " + gameId + " turn " + turn);
            }
            return new JolGame(gameId, gameData);
        }
    }

    public static void rollbackGame(String gameName, String turn) {
        String id = get(gameName).getId();
        JolGame game = loadSnapshot(id, turn);
        saveGameState(game, true);
        INSTANCE.gameCache.refresh(id);
    }

    public static void saveGame(JolGame game) {
        // GameStateEntity is @Version-ed, so a concurrent DB write for the same game that's
        // based on a stale version fails with an optimistic lock conflict. The cache put below
        // is NOT similarly guarded, though: it's a plain put, not a compute(), so two concurrent
        // saveGame() calls can still race on which one's put lands last in the cache.
        String gameName = INSTANCE.idToName.get(game.id());
        if (gameName != null && INSTANCE.games.containsKey(gameName)) {
            INSTANCE.requireJpaWrite(em -> gameStateRepository.save(em, game));
        }
        INSTANCE.gameCache.put(game.id(), game);
    }

    public static void saveGame(JolGame game, String turn) {
        String snapshotTurn = snapshotTurn(turn);
        INSTANCE.requireJpaWrite(em -> gameSnapshotRepository.save(em, game.id(), snapshotTurn, game.data()));
    }

    // turn labels are normalized the same way the legacy game-<turn>.json filenames were
    private static String snapshotTurn(String turn) {
        return turn.replaceAll("\\.", "-");
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

    public static JolGame getGameByName(String gameName) {
        GameInfo gameInfo = get(gameName);
        return INSTANCE.gameCache.get(gameInfo.getId());
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
        // Belt-and-braces: every known GameInfo mutation site goes through updateGameInfo
        // (which write-throughs immediately), but this catches any future direct mutation.
        requireJpaWrite(em -> games.values().forEach(g -> gameInfoRepository.save(em, g)));
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            Map<String, GameInfo> loaded = gameInfoRepository.findAll(em);
            games.putAll(loaded);
            games.forEach((name, info) -> idToName.put(info.getId(), name));
            logger.info("Loaded {} games from JPA", games.size());
        } catch (Exception e) {
            logger.error("JPA load failed for GameService", e);
        }
    }
}
