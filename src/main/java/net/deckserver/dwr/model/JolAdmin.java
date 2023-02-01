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
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String DISCORD_API_VERSION = System.getenv("DISCORD_API_VERSION");
    private static final String DISCORD_BOT_TOKEN = System.getenv("DISCORD_BOT_TOKEN");
    private static final String DISCORD_PING_CHANNEL_ID = System.getenv("DISCORD_PING_CHANNEL_ID");
    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;
    private final Path BASE_PATH;
    private final Map<String, GameInfo> games;
    private final Map<String, PlayerInfo> players;
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();
    private final Table<String, String, DeckInfo> decks = HashBasedTable.create();
    private final ObjectMapper objectMapper;
    private final Timestamps timestamps;
    private final TypeFactory typeFactory;
    private volatile List<ChatEntryBean> chats;
    private final Map<String, GameModel> gmap = new ConcurrentHashMap<>();
    private final Map<String, PlayerModel> pmap = new ConcurrentHashMap<>();

    // Cache of users / status
    private final Cache<String, String> activeUsers = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private final LoadingCache<String, JolGame> gameCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(this::loadGameState);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Predicate<GameInfo> ACTIVE_GAME = (info) -> info.getStatus().equals(GameStatus.ACTIVE);
    private static final Predicate<GameInfo> STARTING_GAME = (info) -> info.getStatus().equals(GameStatus.STARTING);
    private static final Predicate<GameInfo> PUBLIC_GAME = info -> info.getVisibility().equals(Visibility.PUBLIC);
    private static final Predicate<RegistrationStatus> IS_REGISTERED = status -> status.getDeckId() != null;
    private static final Predicate<PlayerGameStatusBean> PLAYER_ACTIVE = player -> !player.isOusted();

    public JolAdmin(String dir) {
        this.BASE_PATH = Paths.get(dir);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        typeFactory = objectMapper.getTypeFactory();
        games = readFile("games.json", typeFactory.constructMapType(ConcurrentHashMap.class, String.class, GameInfo.class));
        players = readFile("players.json", typeFactory.constructMapType(ConcurrentHashMap.class, String.class, PlayerInfo.class));
        chats = readFile("chats.json", typeFactory.constructCollectionType(List.class, ChatEntryBean.class));
        timestamps = readFile("timestamps.json", typeFactory.constructType(Timestamps.class));
        loadRegistrations();
        loadDecks();
        Unirest.setTimeouts(5000, 10000);
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

    private <T> T readFile(String fileName, JavaType type) {
        try {
            logger.info("Reading data from {}", fileName);
            return objectMapper.readValue(BASE_PATH.resolve(fileName).toFile(), type);
        } catch (IOException e) {
            logger.error("Unable to read {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    private void writeFile(String fileName, Object object) {
        try {
            logger.info("Saving data to {}", fileName);
            objectMapper.writeValue(BASE_PATH.resolve(fileName).toFile(), object);
        } catch (IOException e) {
            logger.error("Unable to write {}", fileName, e);
            throw new RuntimeException(e);
        }
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
    }

    public synchronized void cleanupGames() {
        games.values().stream()
                .filter(ACTIVE_GAME)
                .map(GameInfo::getName)
                .map(GameStatusBean::new)
                .forEach(gameStatus -> {
                    long activePlayers = gameStatus.getPlayers().stream().filter(PLAYER_ACTIVE).count();
                    if (activePlayers == 0) {
                        endGame(gameStatus.getName());
                        String message = String.format("{%s} has been closed.", gameStatus.getName());
                        chat("SYSTEM", message);
                    }
                });

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
    }

    public synchronized void buildPublicGames() {
        long publicGamesCount = games.values().stream()
                .filter(ACTIVE_GAME)
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
                games.put(gameName, new GameInfo(gameName, ULID.random(), playerName, Visibility.fromBoolean(isPublic), GameStatus.STARTING));
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

    public void shutdown() {
        try {
            Unirest.shutdown();
            persistState();
            scheduler.shutdownNow();
        } catch (Exception e) {
            logger.error("Unable to cleanly shutdown", e);
        }
    }

    public void setup() {
        scheduler.scheduleAtFixedRate(new PersistStateJob(), 5, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new CleanupGamesJob(), 1, 10, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new PublicGamesBuilderJob(), 1, 1, TimeUnit.HOURS);
    }

    public static JolAdmin getInstance() {
        return INSTANCE;
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

    private ExtendedDeck getDeck(String deckId) {
        return readFile(String.format("decks/%s.json", deckId), typeFactory.constructType(ExtendedDeck.class));
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

    private void copyDeck(String deckId, String gameId) {
        try {
            Path deckPath = BASE_PATH.resolve("decks").resolve(deckId + ".json");
            Path gamePath = BASE_PATH.resolve("games").resolve(gameId).resolve(deckId + ".json");
            logger.info("Copying {} to {}", deckPath, gamePath);
            Files.copy(deckPath, gamePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to load deck for {}", deckId, e);
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

    public JolGame getGame(String gameName) {
        return gameCache.get(gameName);
    }

    private JolGame loadGameState(String gameName) {
        logger.info("Loading {}", gameName);
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
        if (existsPlayer(name) || name.length() == 0)
            return false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        players.put(name, new PlayerInfo(name, ULID.random(), email, hash, null, new HashSet<>()));
        return true;
    }

    public void changePassword(String player, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
        loadPlayerInfo(player).setHash(hash);
    }

    public void updateProfile(String playerName, String email, String discordID) {
        PlayerInfo playerInfo = loadPlayerInfo(playerName);
        playerInfo.setDiscordId(discordID);
        playerInfo.setEmail(email);
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
            registrationStatus.setDeckId(deckInfo.getDeckId());
            registrationStatus.setDeckName(deckInfo.getDeckName());
            registrationStatus.setValid(stats.isValid());
            registrationStatus.setSummary(stats.getSummary());
            copyDeck(deckInfo.getDeckId(), gameInfo.getId());

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

    public static String getDate() {
        return OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
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
            if (player != null && player.getDiscordId() != null) {
                Unirest.post("https://discord.com/api/v{api-version}/channels/{channel-id}/messages")
                        .routeParam("api-version", DISCORD_API_VERSION)
                        .routeParam("channel-id", DISCORD_PING_CHANNEL_ID)
                        .header("Content-type", "application/json")
                        .header("Authorization", String.format("Bot %s", DISCORD_BOT_TOKEN))
                        .body(String.format("{\"content\":\"<@!%s> to %s\"}", player.getDiscordId(), gameName))
                        .asStringAsync(new Callback<>() {
                            public void completed(HttpResponse<String> response) {
                                int responseCode = response.getStatus();
                                if (responseCode != 200) {
                                    logger.warn(
                                            "Non-200 response calling Discord ({}); response body: {}",
                                            String.valueOf(responseCode), response.getBody());
                                }
                            }

                            public void failed(UnirestException e) {
                                logger.error("Error calling Discord", e);
                            }

                            public void cancelled() {
                                logger.warn("Discord call was cancelled");
                            }
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
        registrations.put(gameName, playerName, new RegistrationStatus());
    }

    public boolean isInGame(String gameName, String playerName) {
        return registrations.contains(gameName, playerName);
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
        Path gamePath = BASE_PATH.resolve("games").resolve(gameInfo.getId());
        if (!Files.exists(gamePath)) {
            try {
                logger.info("Creating game directory: {}", gamePath);
                Files.createDirectory(gamePath);
            } catch (IOException e) {
                logger.error("unable to create game directory");
                return;
            }
        }
        registrations.row(gameName).forEach((playerName, registration) -> {
            Deck deck = getDeck(registration.getDeckId()).getDeck();
            if (deck != null) {
                game.addPlayer(playerName, deck);
                Path gameDeckPath = BASE_PATH.resolve("games").resolve(gameInfo.getId()).resolve(registration.getDeckId() + ".json");
                if (!Files.exists(gameDeckPath)) {
                    copyDeck(registration.getDeckId(), gameInfo.getId());
                }
            }
        });
        if (game.getPlayers().size() >= 1 && game.getPlayers().size() <= 5) {
            game.startGame();
            saveGameState(game);
            gameInfo.setStatus(GameStatus.ACTIVE);
        }
    }

    public void endGame(String gameName) {
        GameInfo gameInfo = games.get(gameName);
        Path gamePath = BASE_PATH.resolve("games").resolve(gameInfo.getId());
        registrations.row(gameName).clear();
        games.remove(gameName);
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

}
