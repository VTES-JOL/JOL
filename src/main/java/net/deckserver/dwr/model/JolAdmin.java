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
import lombok.Getter;
import lombok.Setter;
import net.deckserver.DeckParser;
import net.deckserver.RandomGameName;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.dwr.bean.GameStatusBean;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.game.validators.DeckValidator;
import net.deckserver.game.validators.ValidationResult;
import net.deckserver.game.validators.ValidatorFactory;
import net.deckserver.jobs.CleanupGamesJob;
import net.deckserver.jobs.PersistStateJob;
import net.deckserver.jobs.PublicGamesBuilderJob;
import net.deckserver.jobs.ValidateGWJob;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.Timestamps;
import net.deckserver.storage.json.system.*;
import org.apache.commons.io.FileUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class JolAdmin {

    public static final JolAdmin INSTANCE = new JolAdmin(System.getenv("JOL_DATA"));
    private static final Logger logger = LoggerFactory.getLogger(JolAdmin.class);
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
    private static final Predicate<GameInfo> TEST_GAME = info -> info.getOwner().equals("TEST");
    private static final Predicate<DeckInfo> MODERN_DECK = info -> DeckFormat.MODERN.equals(info.getFormat());
    private static final Predicate<DeckInfo> NO_TAGS = info -> info.getGameFormats().isEmpty();
    private static final Predicate<RegistrationStatus> IS_REGISTERED = status -> status.getDeckId() != null;
    private static final DecimalFormat format = new DecimalFormat("0.#");

    static {
        format.setRoundingMode(RoundingMode.DOWN);
    }

    private final Path BASE_PATH;
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();
    private final Table<String, String, DeckInfo> decks = HashBasedTable.create();
    private final ObjectMapper objectMapper;
    private final Map<String, GameModel> gmap = new ConcurrentHashMap<>();
    private final Map<String, PlayerModel> pmap = new ConcurrentHashMap<>();
    // Cache of users / status
    private final Cache<String, String> activeUsers = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    private final HttpClient discord = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<String, GameInfo> games;
    private final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(this::loadGameState);
    private Properties properties;
    private Map<OffsetDateTime, GameHistory> pastGames;
    private Map<String, PlayerInfo> players;
    private TournamentData tournamentRegistrations;
    private Timestamps timestamps;
    private TypeFactory typeFactory;
    @Getter
    @Setter
    private String message;
    @Getter
    private volatile List<ChatEntryBean> chats;

    public JolAdmin(String dir) {
        this.BASE_PATH = Paths.get(dir);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String getDate() {
        return OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
    }

    public boolean isInRole(String playerName, String role) {
        return Optional.ofNullable(players.get(playerName))
                .map(PlayerInfo::getRoles)
                .stream()
                .flatMap(Collection::stream)
                .anyMatch(playerRole -> playerRole.equals(PlayerRole.valueOf(role)));
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
            decksMap.forEach((deckName, deckInfo) -> decks.put(playerName, deckName, deckInfo));
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
        writeFile("tournament.json", tournamentRegistrations);
        writeFile("message.json", message);
    }

    public synchronized void cleanupGames() {
        try {
            logger.debug("CLEAN - Unregistered players");
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

            logger.debug("CLEAN - Close finished games");
            games.values().stream()
                    .filter(ACTIVE_GAME)
                    .filter(TEST_GAME.negate())
                    .map(GameInfo::getName)
                    .map(GameStatusBean::new)
                    .forEach(gameStatus -> {
                        long activePlayers = gameStatus.getActivePlayerCount();
                        if (activePlayers == 0) {
                            endGame(gameStatus.getName(), true);
                            String message = String.format("{%s} has been closed.", gameStatus.getName());
                            chat("SYSTEM", message);
                        }
                    });

            logger.debug("CLEAN - Start ready games");
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
                                endGame(gameName, false);
                            }
                        }
                        if (registeredPlayers > 5) {
                            logger.info("Closing {} : Too many players", gameName);
                            endGame(gameName, false);
                        }
                    });

            logger.debug("CLEAN - Close Idle games and registrations");
            games.values().stream()
                    .filter(STARTING_GAME)
                    .forEach(gameInfo -> {
                        String gameName = gameInfo.getName();
                        OffsetDateTime created = gameInfo.getCreated();
                        if (created != null && created.plusDays(5).isBefore(OffsetDateTime.now())) {
                            logger.info("Closing {} : Idle too long", gameName);
                            if (getRegisteredPlayerCount(gameName) == 4) {
                                startGame(gameName);
                            } else {
                                endGame(gameName, false);
                            }
                        }
                    });

            logger.debug("CLEAN - Timestamps");
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
                if (currentGame != null && notExistsGame(currentGame)) {
                    logger.info("Clearing out closed game {} for {}", playerModel.getCurrentGame(), playerModel.getPlayerName());
                    playerModel.setView("main");
                }
                // clear out games from the recent game view if you are just a spectator and not playing
                playerModel.getCurrentGames().stream()
                        .filter(gameName -> notExistsGame(gameName))
                        .peek(gameName -> logger.info("Removing {} from the list of recent games viewed by {}", gameName, playerModel.getPlayerName()))
                        .forEach(playerModel.getCurrentGames()::remove);
            });

            logger.debug("CLEAN - FINISH");
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
            createGame(gameName, true, GameFormat.STANDARD, "SYSTEM");
            String message = String.format("New public game {%s} has been created.", gameName);
            chat("SYSTEM", message);
        }
    }

    public synchronized PlayerModel getPlayerModel(String name) {
        if (name == null) {
            return new PlayerModel(null, false);
        } else {
            return pmap.computeIfAbsent(name, k -> new PlayerModel(k, true));
        }
    }

    public synchronized GameModel getGameModel(String name) {
        return gmap.computeIfAbsent(name, GameModel::new);
    }

    public Set<String> getWho() {
        return activeUsers.asMap().keySet();
    }

    public void remove(String player) {
        logger.debug("removing player");
        if (player != null) {
            pmap.remove(player);
            for (GameModel gameModel : gmap.values()) {
                gameModel.resetView(player);
            }
        }
    }

    public synchronized void createGame(String gameName, Boolean isPublic, GameFormat format, String playerName) {
        logger.trace("Creating game {} for player {}", gameName, playerName);
        if (gameName.length() > 2 || notExistsGame(gameName)) {
            try {
                String gameId = ULID.random();
                games.put(gameName, new GameInfo(gameName, gameId, playerName, Visibility.fromBoolean(isPublic), GameStatus.STARTING, format));
                Path gamePath = BASE_PATH.resolve("games").resolve(gameId);
                Files.createDirectory(gamePath);
                writeFile("games.json", games);
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

    public void writeSnapshot(String id, DsGame state, DsTurnRecorder actions, String turn) {
        ModelLoader.writeState(id, state, turn);
        ModelLoader.writeActions(id, actions, turn);
    }

    public synchronized void rollbackGame(String gameName, String turn) {
        String id = loadGameInfo(gameName).getId();
        logger.info("Rolling back game {} for turn {}", gameName, turn);
        JolGame game = ModelLoader.loadSnapshot(id, turn);
        game.addMessage("Game state rolled back by Admin.");
        saveGameState(game, true);
        gameCache.refresh(gameName);
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
        typeFactory = objectMapper.getTypeFactory();
        games = readFile("games.json", typeFactory.constructMapType(ConcurrentHashMap.class, String.class, GameInfo.class));
        pastGames = readFile("pastGames.json", typeFactory.constructMapType(ConcurrentHashMap.class, OffsetDateTime.class, GameHistory.class));
        players = readFile("players.json", typeFactory.constructMapType(ConcurrentHashMap.class, String.class, PlayerInfo.class));
        chats = readFile("chats.json", typeFactory.constructCollectionType(List.class, ChatEntryBean.class));
        tournamentRegistrations = readFile("tournament.json", typeFactory.constructType(TournamentData.class));
        timestamps = readFile("timestamps.json", typeFactory.constructType(Timestamps.class));
        message = readFile("message.json", typeFactory.constructType(String.class));
        loadRegistrations();
        loadDecks();
        loadProperties();
        scheduler.scheduleAtFixedRate(new PersistStateJob(), 5, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new CleanupGamesJob(), 0, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new ValidateGWJob(), 0, 1, TimeUnit.DAYS);
        scheduler.scheduleAtFixedRate(new PublicGamesBuilderJob(), 1, 10, TimeUnit.MINUTES);
    }

    public void validate() {
        logger.info("Validating game state");
        List<String> invalidGames = new ArrayList<>();
        games.values().stream().filter(ACTIVE_GAME)
                .forEach(gameInfo -> {
                    try {
                        loadGameState(gameInfo.getName());
                    } catch (Exception e) {
                        logger.error("Unable to validate game {}", gameInfo.getName());
                        invalidGames.add(gameInfo.getName());
                    }
                });
        invalidGames.forEach(games::remove);
        invalidGames.forEach(timestamps::clearGame);

        logger.info("Validating deck state");
        List<Table.Cell<String, String, DeckInfo>> invalidDecks = new ArrayList<>();
        for (var cell : decks.cellSet()) {
            DeckInfo deckInfo = cell.getValue();
            String extension = DeckFormat.LEGACY.equals(deckInfo.getFormat()) ? "txt" : "json";
            String deckFile = String.format("decks/%s.%s", deckInfo.getDeckId(), extension);
            if (!Files.exists(BASE_PATH.resolve(deckFile))) {
                logger.error("Unable to find deck {}", deckFile);
                invalidDecks.add(cell);
            }
        }
        invalidDecks.forEach(cell -> decks.remove(cell.getRowKey(), cell.getColumnKey()));
    }

    public void upgrade() {
        logger.info("Determining upgrades...");
        decks.values().stream().filter(MODERN_DECK).filter(NO_TAGS)
                .forEach(deckInfo -> {
                    ExtendedDeck deck = getDeck(deckInfo.getDeckId());
                    Set<String> tags = ValidatorFactory.getTags(deck.getDeck());
                    deckInfo.setGameFormats(tags);
                    deckInfo.setFormat(DeckFormat.TAGGED);
                    logger.info("Upgrading {} to {} with {} tags", deckInfo.getDeckId(), deckInfo.getFormat(), tags);
                });
    }


    public String getVersion() {
        return properties.getProperty("version");
    }

    public boolean existsPlayer(String name) {
        return name != null && players.containsKey(name);
    }

    public boolean notExistsGame(String name) {
        return name == null || !games.containsKey(name);
    }

    public DeckFormat getDeckFormat(String playerName, String deckName) {
        return loadDeckInfo(playerName, deckName).getFormat();
    }

    public Set<String> getTags(String playerName, String deckName) {
        return loadDeckInfo(playerName, deckName).getGameFormats();
    }

    public synchronized Deck getGameDeck(String gameName, String playerName) {
        return Optional.ofNullable(registrations.get(gameName, playerName))
                .map(status -> {
                    String deckId = status.getDeckId();
                    String gameId = getGameId(gameName);
                    ExtendedDeck extendedDeck = readFile(String.format("games/%s/%s.json", gameId, deckId), typeFactory.constructType(ExtendedDeck.class));
                    return extendedDeck.getDeck();
                }).orElse(null);
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

    public synchronized void selectDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            getPlayerModel(playerName).loadDeck(deckName);
        }
    }

    public synchronized void newDeck(String playerName) {
        if (playerName != null) {
            getPlayerModel(playerName).clearDeck();
        }
    }

    public synchronized void saveDeck(String playerName, String deckName, String contents) {
        if (playerName != null && contents != null && deckName != null) {
            deckName = deckName.trim();
            ExtendedDeck deck = DeckParser.parseDeck(contents);
            deck.getDeck().setName(deckName);
            PlayerModel playerModel = getPlayerModel(playerName);
            playerModel.setDeck(deck);
            playerModel.setContents(contents);
            Set<String> tags = ValidatorFactory.getTags(deck.getDeck());
            DeckInfo deckInfo = Optional.ofNullable(decks.get(playerName, deckName)).orElse(new DeckInfo(ULID.random(), deckName, DeckFormat.TAGGED, tags));
            deckInfo.setFormat(DeckFormat.MODERN);
            deckInfo.setGameFormats(tags);
            decks.put(playerName, deckName, deckInfo);
            writeFile(String.format("decks/%s.json", deckInfo.getDeckId()), deck);
        }
    }

    public synchronized void deleteDeck(String playerName, String deckName) {
        if (playerName != null && deckName != null) {
            getPlayerModel(playerName).clearDeck();
            this.decks.remove(playerName, deckName);
        }
    }

    public synchronized JolGame getGame(String gameName) {
        return gameCache.get(gameName);
    }

    public void saveGameState(JolGame game) {
        saveGameState(game, false);
    }

    public void saveGameState(JolGame game, boolean silent) {
        if (!silent) {
            timestamps.setGameTimestamp(game.getName());
        }
        ModelLoader.saveGame(game);
    }

    public boolean registerPlayer(String name, String password, String email) {
        if (existsPlayer(name) || name.isEmpty())
            return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        players.put(name, new PlayerInfo(name, ULID.random(), email, hash));
        return true;
    }

    public synchronized void changePassword(String player, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        loadPlayerInfo(player).setHash(hash);
    }

    public synchronized void updateProfile(String playerName, String email, String discordID, String veknID) {
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

    public synchronized void registerTournamentMultiDeck(String playerName, String... playerDecks) {
        PlayerInfo playerInfo = players.get(playerName);
        String result = "Successfully registered for tournament";
        int round = 1;
        try {
            List<String> deckNames = new ArrayList<>();
            TournamentRegistration registration = new TournamentRegistration();
            registration.setPlayerName(playerName);
            registration.setVeknId(playerInfo.getVeknId());
            for (String deckName : playerDecks) {
                DeckInfo deckInfo = decks.get(playerName, deckName);
                if (deckInfo == null) {
                    result = "Unable to find deck " + deckName;
                    throw new IllegalStateException(result);
                }
                copyTournamentDeck(playerInfo.getId(), deckInfo.getDeckId(), round++);
                deckNames.add(deckName);
            }
            registration.setDecks(deckNames);
            tournamentRegistrations.getRegistrations().put(playerName, registration);
            writeFile("tournament.json", tournamentRegistrations);
        } catch (IllegalStateException e) {
            logger.error("Error registering tournament deck for {}", playerName, e);
        } finally {
            getPlayerModel(playerName).setMessage(result);
        }
    }

    public void registerTournamentDeck(String gameName, String playerName, String deckName, int round) {
        logger.debug("Registering {} for {}", playerName, gameName);
        PlayerInfo playerInfo = players.get(playerName);
        GameInfo gameInfo = games.get(gameName);
        String deckId = String.format("%s-%d", playerInfo.getId(), round);
        Path deckPath = BASE_PATH.resolve("tournaments").resolve(deckId + ".json");
        assert Files.exists(deckPath);
        try {
            Path gamePath = BASE_PATH.resolve("games").resolve(gameInfo.getId()).resolve(deckId + ".json");
            Files.copy(deckPath, gamePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to load deck for {}", deckId, e);
            throw new RuntimeException(e);
        }
        RegistrationStatus registration = new RegistrationStatus();
        registration.setDeckId(deckId);
        registration.setDeckName(deckName);
        registration.setValid(true);
        registration.setTimestamp(OffsetDateTime.now());
        registrations.put(gameName, playerName, registration);
        writeFile("registrations.json", registrations);
    }

    public synchronized void registerDeck(String gameName, String playerName, String deckName) {
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
            ExtendedDeck extendedDeck = getDeck(deckInfo.getDeckId());
            if (!validateDeck(extendedDeck.getDeck(), gameInfo.getGameFormat()).isValid()) {
                result = "Unable to register deck.  Not valid for defined format.";
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
            registrationStatus.setValid(true);
            registrationStatus.setSummary(extendedDeck.getStats().getSummary());

            // Reset game time to current time to extend idle timeout
            gameInfo.setCreated(OffsetDateTime.now());

            long registeredPlayers = getRegisteredPlayerCount(gameName);
            if (registeredPlayers == gameInfo.getGameFormat().getPlayerCount()) {
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

    public synchronized void recordPlayerAccess(String playerName, String gameName) {
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
            if (discordId != null && !discordId.isBlank()) {
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
                                            responseCode, response.uri(), response.body());
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

    public synchronized Set<String> getPlayers(String gameName) {
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

    public synchronized void setImageTooltipPreference(String player, boolean value) {
        loadPlayerInfo(player).setShowImages(value);
        writeFile("players.json", players);
    }

    public synchronized boolean getImageTooltipPreference(String player) {
        if (player == null) {
            return true;
        }
        return loadPlayerInfo(player).isShowImages();
    }

    public synchronized boolean isAdmin(String player) {
        return loadPlayerInfo(player).getRoles().contains(PlayerRole.ADMIN);
    }

    public synchronized boolean isPlaytester(String player) {
        return loadPlayerInfo(player).getRoles().contains(PlayerRole.PLAYTESTER);
    }

    public synchronized boolean isSuperUser(String playerName) {
        return loadPlayerInfo(playerName).getRoles().contains(PlayerRole.SUPER_USER);
    }

    public synchronized boolean isJudge(String playerName) {
        return loadPlayerInfo(playerName).getRoles().contains(PlayerRole.JUDGE);
    }

    public synchronized String getOwner(String gameName) {
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

    public synchronized void invitePlayer(String gameName, String playerName) {
        registrations.put(gameName, playerName, new RegistrationStatus(OffsetDateTime.now()));
    }

    public synchronized void unInvitePlayer(String gameName, String playerName) {
        if (isStarting(gameName)) {
            registrations.remove(gameName, playerName);
        }
    }

    public synchronized boolean isInGame(String gameName, String playerName) {
        return registrations.contains(gameName, playerName);
    }

    public boolean isRegistered(String gameName, String playerName) {
        return registrations.contains(gameName, playerName) && Objects.requireNonNull(registrations.get(gameName, playerName)).getDeckId() != null;
    }

    public synchronized boolean isStarting(String gameName) {
        return loadGameInfo(gameName).getStatus().equals(GameStatus.STARTING);
    }

    public boolean isActive(String gameName) {
        return loadGameInfo(gameName).getStatus().equals(GameStatus.ACTIVE);
    }

    public boolean isAlive(String gameName, String playerName) {
        return loadGameState(gameName).getPool(playerName) > 0;
    }

    public boolean isPrivate(String gameName) {
        return loadGameInfo(gameName).getVisibility().equals(Visibility.PRIVATE);
    }

    public boolean isPublic(String gameName) {
        return loadGameInfo(gameName).getVisibility().equals(Visibility.PUBLIC);
    }

    public void startGame(String gameName, List<String> players) {
        GameInfo gameInfo = games.get(gameName);
        DsGame state = new DsGame();
        DsTurnRecorder actions = new DsTurnRecorder();
        JolGame game = new JolGame(gameInfo.getId(), state, actions);
        game.initGame(gameName);
        registrations.row(gameName).forEach((playerName, registration) -> {
            if (registration.getDeckId() != null) {
                Deck deck = getGameDeck(gameName, playerName);
                game.addPlayer(playerName, deck);
            }
        });
        if (!game.getPlayers().isEmpty() && game.getPlayers().size() <= 5) {
            game.startGame(players);
            saveGameState(game);
            gameInfo.setStatus(GameStatus.ACTIVE);
        }
        writeFile("games.json", games);
    }

    public synchronized void startGame(String gameName) {
        List<String> players = new ArrayList<>();
        registrations.row(gameName).forEach((playerName, registration) -> {
            if (registration.getDeckId() != null) {
                players.add(playerName);
            }
        });
        Collections.shuffle(players, new SecureRandom());
        startGame(gameName, players);
    }

    public synchronized void endGame(String gameName, boolean graceful) {
        GameInfo gameInfo = games.get(gameName);
        // try and generate stats for game
        if (gameInfo.getStatus().equals(GameStatus.ACTIVE)) {
            JolGame gameData = getGame(gameName);
            if (gameData.getPlayers().size() >= 4 && graceful) {
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
                            topVP = victoryPoints;
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
        gmap.remove(gameName);
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

    public void validateGW() {
        pastGames.values().forEach(gameHistory -> {
            PlayerResult winner = null;
            PlayerResult previousWinner = gameHistory.getResults().stream().filter(PlayerResult::isGameWin).findFirst().orElse(null);
            double topVP = 0.0;
            for (PlayerResult result : gameHistory.getResults()) {
                double victoryPoints = Double.parseDouble(result.getVictoryPoints());
                if (victoryPoints >= 2.0) {
                    if (winner == null) {
                        logger.debug("{} - {} has {} VP and there is no current high score.", gameHistory.getName(), result.getPlayerName(), victoryPoints);
                        winner = result;
                        topVP = victoryPoints;
                    } else if (victoryPoints > topVP) {
                        logger.debug("{} - {} has {} VP, previous high score was {} on {} VP.", gameHistory.getName(), result.getPlayerName(), victoryPoints, winner.getPlayerName(), topVP);
                        winner = result;
                        topVP = victoryPoints;
                    } else if (victoryPoints == topVP) {
                        logger.debug("{} - tie between {} and {}. No winner.", gameHistory.getName(), result.getPlayerName(), winner.getPlayerName());
                        winner = null;
                    }
                }
            }
            if (winner != null && previousWinner == null) {
                logger.info("Found a winner for {} where there wasn't one before, now {} on {}", gameHistory.getName(), winner.getPlayerName(), winner.getVictoryPoints());
                winner.setGameWin(true);
            } else if (winner != null && winner != previousWinner) {
                logger.info("Found a new winner for {}, previously {} on {}, now {} on {}", gameHistory.getName(), previousWinner.getPlayerName(), previousWinner.getVictoryPoints(), winner.getPlayerName(), winner.getVictoryPoints());
                winner.setGameWin(true);
                previousWinner.setGameWin(false);
            }
        });
    }

    public synchronized void replacePlayer(String gameName, String existingPlayer, String newPlayer) {
        RegistrationStatus existingRegistration = registrations.get(gameName, existingPlayer);
        RegistrationStatus newRegistration = registrations.get(gameName, newPlayer);
        // Only replace player if existing player is in the game, and the new player isn't
        if (existingRegistration != null && newRegistration == null) {
            JolGame game = getGame(gameName);
            game.replacePlayer(existingPlayer, newPlayer);
            saveGameState(game);
            getGameModel(gameName).resetView(existingPlayer);
            // Set up the registrations
            registrations.put(gameName, newPlayer, existingRegistration);
            registrations.remove(gameName, existingPlayer);
        }
    }

    public synchronized void deletePLayer(String playerName) {
        Map<String, RegistrationStatus> playerRegistrations = registrations.column(playerName);
        if (playerRegistrations.isEmpty()) {
            logger.info("Deleting unused player {}", playerName);
            archivePlayer(playerName);
        } else {
            logger.info("Unable to delete an active player - {}", playerName);
        }
    }

    public synchronized void setJudge(String playerName, boolean value) {
        PlayerInfo info = players.get(playerName);
        setRole(info, PlayerRole.JUDGE, value);
    }

    public synchronized void setAdmin(String playerName, boolean value) {
        PlayerInfo info = players.get(playerName);
        setRole(info, PlayerRole.ADMIN, value);
    }

    public synchronized void setPlaytester(String playerName, boolean value) {
        PlayerInfo info = players.get(playerName);
        setRole(info, PlayerRole.PLAYTESTER, value);
    }

    public synchronized void setSuperUser(String playerName, boolean value) {
        PlayerInfo info = players.get(playerName);
        setRole(info, PlayerRole.SUPER_USER, value);
    }

    public Collection<TournamentRegistration> getTournamentRegistrations() {
        return tournamentRegistrations.getRegistrations().values();
    }

    public OffsetDateTime getCreatedTime(String gameName) {
        return Optional.ofNullable(games.get(gameName))
                .map(GameInfo::getCreated)
                .orElse(null);
    }

    public void resetView(String playerName) {
        PlayerModel model = getPlayerModel(playerName);
        model.resetChats();
        String currentGame = model.getCurrentGame();
        if (!Strings.isNullOrEmpty(currentGame)) {
            getGameModel(currentGame).resetView(playerName);
        }
    }

    public synchronized void resetView(String playerName, String gameName) {
        getGameModel(gameName).resetView(playerName);
    }

    public boolean isCurrent(String player, String game) {
        OffsetDateTime playerAccess = timestamps.getPlayerAccess(player, game);
        OffsetDateTime gameLastUpdated = timestamps.getGameTimestamp(game);
        return playerAccess.isAfter(gameLastUpdated);
    }

    public synchronized void endTurn(String gameName, String adminName) {
        JolGame game = getGame(gameName);
        game.addMessage("Turn ended by admin " + adminName);
        game.newTurn();
    }

    public synchronized boolean isOwner(String playerName, String gameName) {
        return games.get(gameName).getOwner().equals(playerName);
    }

    public List<String> getPings(String gameName) {
        return timestamps.getGameTimestamps().get(gameName).getPlayerPings().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    public String getFormat(String gameName) {
        return games.get(gameName).getGameFormat().toString();
    }

    public synchronized void validateDeck(String playerName, String contents, GameFormat format) {
        PlayerModel model = getPlayerModel(playerName);
        String deckName = model.getDeck().getDeck().getName();
        ExtendedDeck deck = DeckParser.parseDeck(contents);
        ValidationResult result = validateDeck(deck.getDeck(), format);
        if (result.isValid()) {
            deck.setErrors(List.of("No errors found.  Deck is valid for " + format.getLabel() + "."));
        } else {
            deck.setErrors(result.getErrors());
        }
        deck.getDeck().setName(deckName);
        model.setDeck(deck);
        model.setContents(contents);
    }

    public synchronized List<String> getTurns(String gameName) {
        return gameCache.get(gameName).getTurns().reversed();
    }

    public ExtendedDeck getDeck(String deckId) {
        return readFile(String.format("decks/%s.json", deckId), typeFactory.constructType(ExtendedDeck.class));
    }

    private ValidationResult validateDeck(Deck deck, GameFormat gameFormat) {
        try {
            Constructor<? extends DeckValidator> validatorConstructor = gameFormat.getDeckValidator().getConstructor();
            var validator = validatorConstructor.newInstance();
            return validator.validate(deck);
        } catch (Exception e) {
            logger.error("Could not find constructor for DeckValidator", e);
            throw new RuntimeException(e);
        }
    }

    private void loadProperties() {
        properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream("version.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            logger.error("Unable to load version.properties", e);
            properties.setProperty("version", OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME));
        }
    }

    private void setRole(PlayerInfo info, PlayerRole role, boolean enabled) {
        if (enabled) {
            info.getRoles().add(role);
        } else {
            info.getRoles().remove(role);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readFile(String fileName, JavaType type) {
        try {
            logger.debug("Reading data from {}", fileName);
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

    private void archivePlayer(String playerName) {
        Path archivePath = BASE_PATH.resolve("archives");
        PlayerInfo playerInfo = players.get(playerName);
        Map<String, DeckInfo> playerDecks = decks.row(playerName);
        try {
            if (!Files.exists(archivePath)) {
                Files.createDirectory(archivePath);
            }
            for (DeckInfo deckInfo : playerDecks.values()) {
                String extension = deckInfo.getFormat().equals(DeckFormat.MODERN) ? ".json" : ".txt";
                Path deckPath = BASE_PATH.resolve("decks").resolve(deckInfo.getDeckId() + extension);
                Path archiveDeckPath = archivePath.resolve(deckInfo.getDeckId() + extension);
                Files.move(deckPath, archiveDeckPath);
                logger.info("Archived Deck {}", deckInfo.getDeckId());
            }
            Path archivePlayerPath = archivePath.resolve(playerInfo.getId() + ".json");
            objectMapper.writeValue(archivePlayerPath.toFile(), playerInfo);
            Path archiveDecksPath = archivePath.resolve(playerInfo.getId() + "-decks.json");
            objectMapper.writeValue(archiveDecksPath.toFile(), playerDecks);
            logger.info("Archived Player {}", playerInfo.getId());
        } catch (IOException e) {
            logger.error("Unable to archive player {}", playerInfo.getId(), e);
        }
        players.remove(playerInfo.getName());
    }

    private boolean copyTournamentDeck(String playerId, String deckId, int round) {
        Path tournamentPath = BASE_PATH.resolve("tournaments");
        try {
            if (!Files.exists(tournamentPath)) {
                Files.createDirectory(tournamentPath);
            }
            Path deckPath = BASE_PATH.resolve("decks").resolve(deckId + ".json");
            Path roundPath = BASE_PATH.resolve("tournaments").resolve(String.format("%s-%d.json", playerId, round));
            logger.info("Copying tournament deck {} to {}", deckPath, roundPath);
            Files.copy(deckPath, roundPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            logger.error("Unable to load deck for {}", deckId, e);
            return false;
        }
    }

    private boolean copyDeck(String deckId, String gameId) {
        try {
            Path deckPath = BASE_PATH.resolve("decks").resolve(deckId + ".json");
            Path gamePath = BASE_PATH.resolve("games").resolve(gameId).resolve(deckId + ".json");
            logger.debug("Copying {} to {}", deckPath, gamePath);
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
        return ModelLoader.loadGame(gameId);
    }

}
