/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver.dwr.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.azam.ulidj.ULID;
import net.deckserver.DeckParser;
import net.deckserver.RandomGameName;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.dwr.bean.GameStatusBean;
import net.deckserver.dwr.bean.PlayerGameStatusBean;
import net.deckserver.game.interfaces.state.Game;
import net.deckserver.game.interfaces.turn.TurnRecorder;
import net.deckserver.game.jaxb.XmlFileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.storage.state.StoreGame;
import net.deckserver.game.storage.turn.StoreTurnRecorder;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.jobs.CleanupGamesJob;
import net.deckserver.jobs.PersistStateJob;
import net.deckserver.jobs.PublicGamesBuilderJob;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.DeckStats;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.Timestamps;
import net.deckserver.storage.json.system.*;
import org.apache.commons.io.FileUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

public class JolAdmin {

    private static final Logger logger = getLogger(JolAdmin.class);
    private static final JolAdmin INSTANCE = new JolAdmin(System.getenv("JOL_DATA"));
    private static final String DISCORD_AUTHORIZATION_HEADER = String.format(
            "Bot %s", System.getenv("DISCORD_BOT_TOKEN"));
    private static final URI DISCORD_PING_CHANNEL_URI = URI.create(
            String.format(
                    "https://discord.com/api/v%s/channels/%s/messages",
                    System.getenv("DISCORD_API_VERSION"),
                    System.getenv("DISCORD_PING_CHANNEL_ID")));
    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;
    private static final Predicate<GameInfo> ACTIVE_GAME = (info) -> info.getStatus().equals(GameStatus.ACTIVE);
    private static final Predicate<GameInfo> STARTING_GAME = (info) -> info.getStatus().equals(GameStatus.STARTING);
    private static final Predicate<GameInfo> PUBLIC_GAME = info -> info.getVisibility().equals(Visibility.PUBLIC);
    private static final Predicate<RegistrationStatus> IS_REGISTERED = status -> status.getDeckId() != null;
    private static final Predicate<PlayerGameStatusBean> PLAYER_ACTIVE = player -> !player.isOusted();
    private static final DecimalFormat format = new DecimalFormat("0.#");
    private static DateTimeFormatter SIMPLE_FORMAT = DateTimeFormatter.ofPattern("d-MMM HH:mm ");

    static {
        format.setRoundingMode(RoundingMode.DOWN);
    }

    private final Path BASE_PATH;
    private final Map<String, GameInfo> games;
    private final Map<OffsetDateTime, GameHistory> pastGames;
    private final Map<String, PlayerInfo> players;
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();
    private final Table<String, String, DeckInfo> decks = HashBasedTable.create();
    private final ObjectMapper objectMapper;
    private final Timestamps timestamps;
    private final TypeFactory typeFactory;
    private final Map<String, GameModel> gmap = new ConcurrentHashMap<>();
    private final Map<String, PlayerModel> pmap = new ConcurrentHashMap<>();
    // Cache of users / status
    private final Cache<String, String> activeUsers = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    private final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(this::loadGameState);
    private final HttpClient discord = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile List<ChatEntryBean> chats;

    public JolAdmin(String dir) {
        this.BASE_PATH = Paths.get(dir);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        typeFactory = objectMapper.getTypeFactory();
        games = readFile("games.json", typeFactory.constructMapType(ConcurrentHashMap.class, String.class, GameInfo.class));
        pastGames = readFile("pastGames.json", typeFactory.constructMapType(ConcurrentHashMap.class, OffsetDateTime.class, GameHistory.class));
        players = readFile("players.json", typeFactory.constructMapType(ConcurrentHashMap.class, String.class, PlayerInfo.class));
        chats = readFile("chats.json", typeFactory.constructCollectionType(List.class, ChatEntryBean.class));
        timestamps = readFile("timestamps.json", typeFactory.constructType(Timestamps.class));
        loadRegistrations();
        loadDecks();
    }

    public static JolAdmin getInstance() {
        return INSTANCE;
    }

    public static String getDate() {
        return OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
    }

    public int getRefreshInterval(String gameName) {
        OffsetDateTime lastChanged = getGameTimeStamp(gameName);
        OffsetDateTime now = OffsetDateTime.now();
        long interval = Duration.between(lastChanged, now).getSeconds();
        if (interval < 60) return 5000;
        if (interval < 180) return 10000;
        if (interval < 300) return 30000;
        return 60000;
    }

    public Map<OffsetDateTime, GameHistory> getHistory() {
        return this.pastGames;
    }

    public void loadRegistrations() {
        MapType registrationMapType = typeFactory.constructMapType(Map.class, String.class, RegistrationStatus.class);
        Map<String, Map<String, RegistrationStatus>> registrationsMap = readFile("registrations.json", typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), registrationMapType));
        registrationsMap.forEach((gameId, gameMap) -> {
            gameMap.forEach((playerId, registration) -> {
                registrations.put(gameId, playerId, registration);
            });
        });
    }

    public void loadDecks() {
        MapType deckMapType = typeFactory.constructMapType(Map.class, String.class, DeckInfo.class);
        Map<String, Map<String, DeckInfo>> decksMapFile = readFile("decks.json", typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), deckMapType));
        decksMapFile.forEach((playerName, decksMap) -> {
            decksMap.forEach((deckName, deckInfo) -> {
                decks.put(playerName, deckName, deckInfo);
            });
        });
    }

    public synchronized void persistState() {
        writeFile("chats.json", chats);
        writeFile("timestamps.json", timestamps);
        writeFile("games.json", games);
        writeFile("players.json", players);
        writeFile("registrations.json", registrations);
        writeFile("decks.json", decks);
        writeFile("pastGames.json", pastGames);
    }

    public synchronized void cleanupGames() {
        try {
            logger.info("CLEAN - Unregistered players");
            Table<String, String, Boolean> invalidRegistrations = HashBasedTable.create();
            games.values().stream()
                    .filter(ACTIVE_GAME)
                    .map(GameInfo::getName)
                    .forEach(gameName -> {
                        Map<String, RegistrationStatus> playerRegistrations = registrations.row(gameName);
                        playerRegistrations.forEach((player, registration) -> {
                            if (!isRegistered(gameName, player)) {
                                logger.info("Removing unregistered player {} from active game {}", player, gameName);
                                invalidRegistrations.put(gameName, player, Boolean.TRUE);
                            }
                        });
                    });

            logger.info("CLEAN - Close finished games");
            games.values().stream()
                    .filter(ACTIVE_GAME)
                    .map(GameInfo::getName)
                    .map(GameStatusBean::new)
                    .forEach(gameStatus -> {
                        long activePlayers = gameStatus.getActivePlayerCount();
                        if (activePlayers == 0) {
                            endGame(gameStatus.getName());
                            String message = String.format("{%s} has been closed.", gameStatus.getName());
                            chat("SYSTEM", message);
                        }
                    });

            logger.info("CLEAN - Start ready games");
            games.values().stream()
                    .filter(STARTING_GAME)
                    .map(GameInfo::getName)
                    .forEach(gameName -> {
                        long registeredPlayers = getRegisteredPlayerCount(gameName);
                        if (registeredPlayers == 5) {
                            try {
                                startGame(gameName);
                                logger.info("Started {}.", gameName);
                            } catch (Exception e) {
                                logger.error("Something went wrong starting {}", gameName, e);
                                endGame(gameName);
                            }
                        }
                        if (registeredPlayers > 5) {
                            logger.info("Closing {} : Too many players", gameName);
                            endGame(gameName);
                        }
                    });

            logger.info("CLEAN - Close Idle games and registrations");
            games.values().stream()
                    .filter(STARTING_GAME)
                    .forEach(gameInfo -> {
                        String gameName = gameInfo.getName();
                        OffsetDateTime created = gameInfo.getCreated();
                        if (created != null && created.plusDays(3).isBefore(OffsetDateTime.now())) {
                            logger.info("Closing {} : Idle too long", gameName);
                            endGame(gameName);
                        }
                        registrations.row(gameName).forEach((playerName, status) -> {
                            if (status.getTimestamp() != null && status.getTimestamp().plusDays(1).isBefore(OffsetDateTime.now())) {
                                logger.info("Removing idle player {} from starting game {}", playerName, gameName);
                                invalidRegistrations.put(gameName, playerName, Boolean.TRUE);
                            }
                        });
                    });

            logger.info("CLEAN - Timestamps");
            Set<String> timestampGames = new HashSet<>();
            timestamps.getGameTimestamps().keySet().forEach(gameName -> {
                if (!games.containsKey(gameName)) {
                    logger.info("Removing {} timestamp record", gameName);
                    timestampGames.add(gameName);
                }
            });

            timestampGames.forEach(timestamps::clearGame);

            invalidRegistrations.cellSet().forEach(cell -> {
                String game = cell.getRowKey();
                String player = cell.getColumnKey();
                registrations.remove(game, player);
            });

            // clear out any empty player/game names in registrations
            if (registrations.containsColumn("")) {
                logger.info("Removing bad player data from registrations");
                registrations.column("").clear();
            }
            if (registrations.containsRow("")) {
                logger.info("Removing bad game data from registrations");
                registrations.row("").clear();
            }

            pmap.values().forEach(playerModel -> {
                // Check current game exists still, this is the cause of people unable to login sometimes
                // when the player model in session thinks they are in a game that's since been closed
                String currentGame = playerModel.getCurrentGame();
                if (currentGame != null && !existsGame(currentGame)) {
                    logger.info("Clearing out closed game {} for {}", playerModel.getCurrentGame(), playerModel.getPlayerName());
                    playerModel.setView("main");
                }
                // clear out games from the recent game view if you are just a spectator and not playing
                playerModel.getCurrentGames().stream()
                        .filter(gameName -> !existsGame(gameName))
                        .peek(gameName -> logger.info("Removing {} from the list of recent games viewed by {}", gameName, playerModel.getPlayerName()))
                        .forEach(playerModel.getCurrentGames()::remove);
            });

            logger.info("CLEAN - FINISH");
            persistState();

        } catch (Exception e) {
            logger.error("Caught runtime exception", e);
        }
    }

    public synchronized void buildPublicGames() {
        long publicGamesCount = games.values().stream()
                .filter(STARTING_GAME)
                .filter(PUBLIC_GAME)
                .count();
        long gamesNeeded = 5 - publicGamesCount;
        for (int x = 0; x < gamesNeeded; x++) {
            String gameName = RandomGameName.generateName();
            createGame(gameName, true, "SYSTEM");
            String message = String.format("New public game {%s} has been created.", gameName);
            chat("SYSTEM", message);
        }
    }

    public PlayerModel getPlayerModel(String name) {
        if (name == null) {
            return new PlayerModel(null, false);
        } else {
            PlayerModel bean = pmap.get(name);
            if (bean == null) {
                logger.info("Creating new Player model for {}", name);
                bean = new PlayerModel(name, true);
                pmap.put(name, bean);
            }
            return bean;
        }
    }

    public GameModel getGameModel(String name) {
        GameModel bean = gmap.get(name);
        if (bean == null) {
            bean = new GameModel(name);
            gmap.put(name, bean);
        }
        return bean;
    }

    public Set<String> getWho() {
        return activeUsers.asMap().keySet();
    }

    public void remove(String player) {
        logger.info("removing player");
        pmap.remove(player);
        for (GameModel gameModel : gmap.values()) {
            gameModel.resetView(player);
        }
    }

    public synchronized void createGame(String gameName, Boolean isPublic, String playerName) {
        logger.trace("Creating game {} for player {}", gameName, playerName);
        if (gameName.length() > 2 || !existsGame(gameName)) {
            try {
                String gameId = ULID.random();
                games.put(gameName, new GameInfo(gameName, gameId, playerName, Visibility.fromBoolean(isPublic), GameStatus.STARTING));
                Path gamePath = BASE_PATH.resolve("games").resolve(gameId);
                Files.createDirectory(gamePath);
            } catch (Exception e) {
                logger.error("Error creating game", e);
            }
        }
    }

    public synchronized void chat(String player, String message) {
        String sanitize = ChatParser.sanitizeText(message);
        String parsedMessage = ChatParser.parseText(sanitize);
        ChatEntryBean chatEntryBean = new ChatEntryBean(player, parsedMessage);
        chats.add(chatEntryBean);
        if (chats.size() > CHAT_STORAGE) {
            chats = chats.subList(CHAT_DISCARD, CHAT_STORAGE);
        }
        pmap.values()
                .forEach(playerModel -> playerModel.chat(chatEntryBean));
    }

    public List<ChatEntryBean> getChats() {
        return this.chats;
    }

    public void writeSnapshot(String id, DsGame state, DsTurnRecorder actions, String turn) {
        writeState(id, state, turn);
        writeActions(id, actions, turn);
    }

    public void shutdown() {
        try {
            persistState();
            scheduler.shutdownNow();
        } catch (Exception e) {
            logger.error("Unable to cleanly shutdown", e);
        }
    }

    public void setup() {
        scheduler.scheduleAtFixedRate(new PersistStateJob(), 5, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new CleanupGamesJob(), 0, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new PublicGamesBuilderJob(), 10, 1, TimeUnit.MINUTES);
    }

    public boolean existsPlayer(String name) {
        return name != null && players.containsKey(name);
    }

    public boolean existsGame(String name) {
        return name != null && games.containsKey(name);
    }

    public DeckFormat getDeckFormat(String playerName, String deckName) {
        return loadDeckInfo(playerName, deckName).getFormat();
    }

    public Deck getGameDeck(String gameName, String playerName) {
        RegistrationStatus status = registrations.get(gameName, playerName);
        try {
            String deckId = status.getDeckId();
            String gameId = getGameId(gameName);
            ExtendedDeck extendedDeck = readFile(String.format("games/%s/%s.json", gameId, deckId), typeFactory.constructType(ExtendedDeck.class));
            return extendedDeck.getDeck();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String getDeckContents(String deckId) throws IOException {
        ExtendedDeck deck = getDeck(deckId);
        StringBuilder builder = new StringBuilder();
        Consumer<CardCount> itemBuilder = cardCount -> builder.append(cardCount.getCount()).append(" x ").append(cardCount.getName()).append("\n");
        deck.getDeck().getCrypt().getCards().forEach(itemBuilder);
        builder.append("\n");
        deck.getDeck().getLibrary().getCards().forEach(libraryCard -> libraryCard.getCards().forEach(itemBuilder));
        return builder.toString();
    }

    public String getLegacyContents(String deckId) throws IOException {
        Path deckPath = BASE_PATH.resolve("decks").resolve(deckId + ".txt");
        return Files.readString(deckPath);
    }

    public void selectDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            getPlayerModel(playerName).loadDeck(deckName);
        }
    }

    public void parseDeck(String playerName, String deckName, String contents) {
        if (playerName != null && contents != null) {
            getPlayerModel(playerName).setContents(contents);
            getPlayerModel(playerName).setDeckName(deckName);
        }
    }

    public void newDeck(String playerName) {
        if (playerName != null) {
            getPlayerModel(playerName).clearDeck();
        }
    }

    public void saveDeck(String playerName, String deckName, String contents) {
        if (playerName != null && contents != null && deckName != null) {
            PlayerModel model = getPlayerModel(playerName);
            model.setDeckName(deckName);
            model.setContents(contents);
            ExtendedDeck deck = DeckParser.parseDeck(contents);
            DeckInfo deckInfo;
            if (decks.contains(playerName, deckName)) {
                deckInfo = decks.get(playerName, deckName);
                deckInfo.setFormat(DeckFormat.MODERN);
            } else {
                deckInfo = new DeckInfo(ULID.random(), deckName, DeckFormat.MODERN);
            }
            decks.put(playerName, deckName, deckInfo);
            writeFile(String.format("decks/%s.json", deckInfo.getDeckId()), deck);
        }
    }

    public void deleteDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            getPlayerModel(playerName).clearDeck();
            this.decks.remove(playerName, deckName);
        }
    }

    public JolGame getGame(String gameName) {
        return gameCache.get(gameName);
    }

    public void saveGameState(JolGame game) {
        logger.info("Saving {}", game.getName());
        timestamps.setGameTimestamp(game.getName());
        String gameId = game.getId();
        Path gameStatePath = BASE_PATH.resolve("games").resolve(gameId).resolve("game.xml");
        Path gameActionsPath = BASE_PATH.resolve("games").resolve(gameId).resolve("actions.xml");
        Game deckServerState = game.getState();
        TurnRecorder deckServerActions = game.getTurnRecorder();
        GameState gameState = new GameState();
        GameActions gameActions = new GameActions();
        gameActions.setCounter("1");
        gameActions.setGameCounter("1");
        ModelLoader.createModel(new StoreGame(gameState), deckServerState);
        ModelLoader.createRecorder(new StoreTurnRecorder(gameActions), deckServerActions);
        XmlFileUtils.saveGameState(gameState, gameStatePath);
        XmlFileUtils.saveGameActions(gameActions, gameActionsPath);
    }

    public boolean registerPlayer(String name, String password, String email) {
        if (existsPlayer(name) || name.isEmpty())
            return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        players.put(name, new PlayerInfo(name, ULID.random(), email, hash, null, null, new HashSet<>()));
        return true;
    }

    public void changePassword(String player, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        loadPlayerInfo(player).setHash(hash);
    }

    public void updateProfile(String playerName, String email, String discordID, String veknID) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        playerInfo.setDiscordId(discordID);
        playerInfo.setEmail(email);
        playerInfo.setVeknId(veknID);
    }

    public boolean authenticate(String playerName, String password) {
        if (existsPlayer(playerName)) {
            PlayerInfo playerInfo = loadPlayerInfo(playerName);
            return BCrypt.checkpw(password, playerInfo.getHash());
        } else {
            return false;
        }
    }

    public long getRegisteredPlayerCount(String gameName) {
        return registrations.row(gameName).values().stream().filter(IS_REGISTERED).count();
    }

    public RegistrationStatus getRegistration(String gameName, String playerName) {
        return registrations.get(gameName, playerName);
    }

    public void registerDeck(String gameName, String playerName, String deckName) {
        deckName = deckName.trim();
        DeckInfo deckInfo = decks.get(playerName, deckName);
        GameInfo gameInfo = loadGameInfo(gameName);
        String result = "Successfully registered " + deckName + " in game " + gameName;
        try {
            if (!gameInfo.getStatus().equals(GameStatus.STARTING)) {
                result = "Game is not starting.  Unable to register deck.";
                throw new IllegalStateException(result);
            }
            if (deckInfo == null) {
                result = "Unable to find deck '" + deckName + "'.";
                throw new IllegalStateException(result);
            }
            if (deckInfo.getFormat().equals(DeckFormat.LEGACY)) {
                result = "Unable to register legacy formats in new games.  Please edit, and save deck to convert to new format.";
                throw new IllegalStateException(result);
            }
            if (getRegisteredPlayerCount(gameName) >= 5) {
                result = "Unable to register deck.  Already has 5 players registered.";
                throw new IllegalStateException(result);
            }
            DeckStats stats = getDeck(deckInfo.getDeckId()).getStats();
            if (stats == null || !stats.isValid()) {
                result = "Unable to register deck.  Not valid.";
                throw new IllegalStateException(result);
            }
            RegistrationStatus registrationStatus = registrations.get(gameName, playerName);
            if (registrationStatus == null) {
                result = "Unable to register deck in game that has no invite";
                throw new IllegalStateException(result);
            }
            boolean copySuccess = copyDeck(deckInfo.getDeckId(), gameInfo.getId());
            if (!copySuccess) {
                result = "Unable to copy deck file to game";
                throw new IllegalStateException(result);
            }
            registrationStatus.setDeckId(deckInfo.getDeckId());
            registrationStatus.setDeckName(deckInfo.getDeckName());
            registrationStatus.setValid(stats.isValid());
            registrationStatus.setSummary(stats.getSummary());

            gameInfo.setCreated(OffsetDateTime.now());

            long registeredPlayers = getRegisteredPlayerCount(gameName);
            if (registeredPlayers == 5) {
                startGame(gameName);
            }
        } catch (IllegalStateException exception) {
            logger.debug(exception.getMessage());
        } finally {
            getPlayerModel(playerName).setMessage(result);
        }
    }

    public void recordPlayerAccess(String playerName) {
        if (playerName != null) {
            this.timestamps.recordPlayerAccess(playerName);
            this.activeUsers.put(playerName, OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        }
    }

    public void recordPlayerAccess(String playerName, String gameName) {
        this.timestamps.recordPlayerAccess(playerName, gameName);
    }

    public OffsetDateTime getGameTimeStamp(String gameName) {
        return this.timestamps.getGameTimestamp(gameName);
    }

    public OffsetDateTime getPlayerAccess(String playerName) {
        return this.timestamps.getPlayerAccess(playerName);
    }

    public OffsetDateTime getPlayerAccess(String playerName, String gameName) {
        return this.timestamps.getPlayerAccess(playerName, gameName);
    }

    public boolean isPlayerPinged(String playerName, String gameName) {
        return this.timestamps.isPlayerPinged(playerName, gameName);
    }

    public boolean pingPlayer(String playerName, String gameName) {
        if (isPlayerPinged(playerName, gameName)) {
            logger.debug("{} already pinged for {}; not pinging again", playerName, gameName);
            return false;
        }

        this.timestamps.pingPlayer(playerName, gameName);

        //Ping on Discord
        PlayerInfo player = loadPlayerInfo(playerName);
        try {
            String discordId = player.getDiscordId();
            if (player != null && discordId != null && !discordId.isBlank()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(DISCORD_PING_CHANNEL_URI)
                        .header("Content-type", "application/json")
                        .header("Authorization", DISCORD_AUTHORIZATION_HEADER)
                        .timeout(Duration.ofSeconds(10))
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        String.format("{\"content\":\"<@!%s> to %s\"}", player.getDiscordId(), gameName)))
                        .build();
                discord.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .handle((response, exception) -> {
                            if (exception == null) {
                                int responseCode = response.statusCode();
                                if (responseCode != 200) {
                                    logger.warn(
                                            "Non-200 response ({}) calling Discord ({}); response body: {}",
                                            String.valueOf(responseCode), response.uri(), response.body());
                                }
                            } else logger.error("Error calling Discord", exception);
                            return null;
                        });
            }
        } catch (Exception e) {
            logger.error("Unable to ping player", e);
        }
        return true;
    }

    public void clearPing(String playerName, String gameName) {
        this.timestamps.clearPing(playerName, gameName);
    }

    public Set<String> getGames() {
        return games.keySet();
    }

    public Set<String> getGames(String playerName) {
        return registrations.column(playerName).keySet();
    }

    public Set<String> getPlayers(String gameName) {
        return registrations.row(gameName).keySet();
    }

    public String getEmail(String player) {
        return loadPlayerInfo(player).getEmail();
    }

    public String getDiscordID(String player) {
        return loadPlayerInfo(player).getDiscordId();
    }

    public String getVeknID(String player) {
        return loadPlayerInfo(player).getVeknId();
    }

    public boolean isAdmin(String player) {
        return loadPlayerInfo(player).getRoles().contains(PlayerRole.ADMIN);
    }

    public boolean isSuperUser(String playerName) {
        return loadPlayerInfo(playerName).getRoles().contains(PlayerRole.SUPER_USER);
    }

    public boolean isJudge(String playerName) {
        return loadPlayerInfo(playerName).getRoles().contains(PlayerRole.JUDGE);
    }

    public String getOwner(String gameName) {
        String playerName = loadGameInfo(gameName).getOwner();
        if (!players.containsKey(playerName)) {
            playerName = "SYSTEM";
        }
        return playerName;
    }

    public Set<String> getDeckNames(String playerName) {
        return decks.row(playerName).keySet();
    }

    public Set<String> getPlayers() {
        return players.keySet();
    }

    public void invitePlayer(String gameName, String playerName) {
        registrations.put(gameName, playerName, new RegistrationStatus(OffsetDateTime.now()));
    }

    public void unInvitePlayer(String gameName, String playerName) {
        if (isStarting(gameName)) {
            registrations.remove(gameName, playerName);
        }
    }

    public boolean isInGame(String gameName, String playerName) {
        return registrations.contains(gameName, playerName);
    }

    public boolean isRegistered(String gameName, String playerName) {
        return registrations.contains(gameName, playerName) && registrations.get(gameName, playerName).getDeckId() != null;
    }

    public boolean isStarting(String gameName) {
        return loadGameInfo(gameName).getStatus().equals(GameStatus.STARTING);
    }

    public boolean isActive(String gameName) {
        return loadGameInfo(gameName).getStatus().equals(GameStatus.ACTIVE);
    }

    public boolean isPrivate(String gameName) {
        return loadGameInfo(gameName).getVisibility().equals(Visibility.PRIVATE);
    }

    public boolean isPublic(String gameName) {
        return loadGameInfo(gameName).getVisibility().equals(Visibility.PUBLIC);
    }

    public void startGame(String gameName) {
        GameInfo gameInfo = games.get(gameName);
        DsGame state = new DsGame();
        DsTurnRecorder actions = new DsTurnRecorder();
        JolGame game = new JolGame(gameInfo.getId(), state, actions);
        game.initGame(gameName);
        registrations.row(gameName).forEach((playerName, registration) -> {
            if (registration.getDeckId() != null) {
                Deck deck = getDeck(registration.getDeckId()).getDeck();
                game.addPlayer(playerName, deck);
            }
        });
        if (!game.getPlayers().isEmpty() && game.getPlayers().size() <= 5) {
            game.startGame();
            saveGameState(game);
            gameInfo.setStatus(GameStatus.ACTIVE);
        }
    }

    public void endGame(String gameName) {
        GameInfo gameInfo = games.get(gameName);
        // try and generate stats for game
        if (gameInfo.getStatus().equals(GameStatus.ACTIVE)) {
            JolGame gameData = getGame(gameName);
            if (gameData.getPlayers().size() > 4) {
                GameHistory history = new GameHistory();
                history.setName(gameName);
                String startTime = gameInfo.getCreated() != null ? gameInfo.getCreated().format(ISO_OFFSET_DATE_TIME) : " --- ";
                String endTime = OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
                history.setStarted(startTime);
                history.setEnded(endTime);
                PlayerResult winner = null;
                double topVP = 0.0;
                boolean hasVp = false;
                for (String player : gameData.getPlayers()) {
                    PlayerResult result = new PlayerResult();
                    String deckName = Optional.ofNullable(registrations.get(gameName, player)).map(RegistrationStatus::getDeckName).orElse("-- no deck name --");
                    double victoryPoints = gameData.getVictoryPoints(player);
                    if (victoryPoints > 0) {
                        hasVp = true;
                    }
                    result.setPlayerName(player);
                    result.setDeckName(deckName);
                    result.setVictoryPoints(format.format(victoryPoints));
                    if (victoryPoints >= 2.0) {
                        if (winner == null) {
                            winner = result;
                            topVP = victoryPoints;
                        } else if (victoryPoints > topVP) {
                            winner = result;
                        } else {
                            winner = null;
                        }
                    }
                    history.getResults().add(result);
                }
                if (winner != null) {
                    winner.setGameWin(true);
                }
                if (hasVp) {
                    pastGames.put(OffsetDateTime.now(), history);
                }
            }
        }
        // Clear out data
        Path gamePath = BASE_PATH.resolve("games").resolve(gameInfo.getId());
        registrations.row(gameName).clear();
        games.remove(gameName);
        timestamps.getGameTimestamps().remove(gameName);
        pmap.values().forEach(playerModel -> playerModel.removeGame(gameName));
        try {
            FileUtils.deleteDirectory(gamePath.toFile());
        } catch (IOException e) {
            logger.error("Unable to delete game directory", e);
        }
    }

    public String getDeckId(String playerName, String deckName) {
        return decks.get(playerName, deckName).getDeckId();
    }

    public String getGameId(String gameName) {
        return loadGameInfo(gameName).getId();
    }

    private <T> T readFile(String fileName, JavaType type) {
        try {
            logger.info("Reading data from {}", fileName);
            return objectMapper.readValue(BASE_PATH.resolve(fileName).toFile(), type);
        } catch (IOException e) {
            logger.error("Unable to read {}", fileName, e);
            try {
                return (T) type.getRawClass().getConstructor().newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException nsm) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeFile(String fileName, Object object) {
        try {
            logger.debug("Saving data to {}", fileName);
            objectMapper.writeValue(BASE_PATH.resolve(fileName).toFile(), object);
        } catch (IOException e) {
            logger.error("Unable to write {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    private void writeActions(String id, DsTurnRecorder actions, String turn) {
        GameActions gactions = new GameActions();
        gactions.setCounter("1");
        gactions.setGameCounter("1");
        StoreTurnRecorder wrec = new StoreTurnRecorder(gactions);
        ModelLoader.createRecorder(wrec, actions);
        String fileName = Strings.isNullOrEmpty(turn) ? "actions.xml" : "actions-" + turn + ".xml";
        Path actionsPath = BASE_PATH.resolve("games").resolve(id).resolve(fileName);
        XmlFileUtils.saveGameActions(gactions, actionsPath);
    }

    private void writeState(String id, DsGame state, String turn) {
        GameState gstate = new GameState();
        StoreGame wgame = new StoreGame(gstate);
        ModelLoader.createModel(wgame, state);
        String fileName = Strings.isNullOrEmpty(turn) ? "game.xml" : "game-" + turn + ".xml";
        Path gamePath = BASE_PATH.resolve("games").resolve(id).resolve(fileName);
        XmlFileUtils.saveGameState(gstate, gamePath);
    }

    private ExtendedDeck getDeck(String deckId) {
        return readFile(String.format("decks/%s.json", deckId), typeFactory.constructType(ExtendedDeck.class));
    }

    private boolean copyDeck(String deckId, String gameId) {
        try {
            Path deckPath = BASE_PATH.resolve("decks").resolve(deckId + ".json");
            Path gamePath = BASE_PATH.resolve("games").resolve(gameId).resolve(deckId + ".json");
            logger.info("Copying {} to {}", deckPath, gamePath);
            Files.copy(deckPath, gamePath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            logger.error("Unable to load deck for {}", deckId, e);
            return false;
        }
    }

    private DeckInfo loadDeckInfo(String playerName, String deckName) {
        return decks.get(playerName, deckName);
    }

    private PlayerInfo loadPlayerInfo(String playerName) {
        if (players.containsKey(playerName)) {
            return players.get(playerName);
        }
        throw new IllegalArgumentException("Player: " + playerName + " was not found.");
    }

    private GameInfo loadGameInfo(String gameName) {
        if (games.containsKey(gameName)) {
            return games.get(gameName);
        }
        throw new IllegalArgumentException("Game: " + gameName + " was not found.");
    }

    private JolGame loadGameState(String gameName) {
        logger.debug("Loading {}", gameName);
        GameInfo gameInfo = loadGameInfo(gameName);
        String gameId = gameInfo.getId();
        Path gameStatePath = BASE_PATH.resolve("games").resolve(gameId).resolve("game.xml");
        GameState gameState = XmlFileUtils.loadGameState(gameStatePath);
        Path gameActionsPath = BASE_PATH.resolve("games").resolve(gameId).resolve("actions.xml");
        GameActions gameActions = XmlFileUtils.loadGameActions(gameActionsPath);
        DsGame deckServerState = new DsGame();
        DsTurnRecorder deckServerActions = new DsTurnRecorder();
        ModelLoader.createModel(deckServerState, new StoreGame(gameState));
        ModelLoader.createRecorder(deckServerActions, new StoreTurnRecorder(gameActions));
        return new JolGame(gameId, deckServerState, deckServerActions);
    }

}
