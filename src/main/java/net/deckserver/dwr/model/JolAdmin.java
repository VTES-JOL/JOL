/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver.dwr.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.deckserver.game.jaxb.FileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.storage.state.StoreGame;
import net.deckserver.game.storage.turn.StoreTurnRecorder;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;
import net.deckserver.storage.json.game.Timestamps;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Joe User
 */
public class JolAdmin {

    public static final String DECK_NOT_FOUND = "Deck not found";
    private static final String DISCORD_API_VERSION = System.getenv("DISCORD_API_VERSION");
    private static final String DISCORD_BOT_TOKEN = System.getenv("DISCORD_BOT_TOKEN");
    private static final String DISCORD_PING_CHANNEL_ID = System.getenv("DISCORD_PING_CHANNEL_ID");

    private static final Logger logger = getLogger(JolAdmin.class);
    private static final JolAdmin INSTANCE = new JolAdmin(System.getenv("JOL_DATA"));
    private final String dir;
    private final SystemInfo sysInfo;
    private final Map<String, GameInfo> games;
    private final Map<String, PlayerInfo> players;
    private final ObjectMapper objectMapper;
    private final Timestamps timestamps;

    public JolAdmin(String dir) {
        this.dir = dir;
        games = new HashMap<>();
        players = new HashMap<>();
        sysInfo = new SystemInfo();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        timestamps = loadTimestamps();
        Unirest.setTimeouts(5000, 10000);
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
        String fileName = Strings.isNullOrEmpty(turn) ? "actions.xml" : "actions-"+turn+".xml";
        File actionsFile = Paths.get(dir).resolve(id).resolve(fileName).toFile();
        FileUtils.saveGameActions(gactions, actionsFile);
    }

    private void writeState(String id, DsGame state, String turn) {
        GameState gstate = new GameState();
        StoreGame wgame = new StoreGame(gstate);
        ModelLoader.createModel(wgame, state);
        String fileName = Strings.isNullOrEmpty(turn) ? "game.xml" : "game-"+turn+".xml";
        File gameFile = Paths.get(dir).resolve(id).resolve(fileName).toFile();
        FileUtils.saveGameState(gstate, gameFile);
    }

    private Timestamps loadTimestamps() {
        try {
            return objectMapper.readValue(new File(dir, "timestamps.json"), Timestamps.class);
        } catch (IOException e) {
            return new Timestamps();
        }
    }

    public void shutdown() {
        try {
            saveTimestamps();
            Unirest.shutdown();
        } catch (Exception e) {
            logger.error("Unable to cleanly shutdown", e);
        }
    }

    private void saveTimestamps() throws Exception {
        logger.info("Saving timestamp data");
        objectMapper.writeValue(new File(dir, "timestamps.json"), timestamps);
    }

    public static JolAdmin getInstance() {
        return INSTANCE;
    }

    private synchronized static String readFile(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes);
    }

    private synchronized static String readIsoFile(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, Charset.forName("ISO8859-1"));
    }


    private synchronized static void writeFile(File file, String contents)
            throws IOException {
        byte[] bytes = contents.getBytes("utf-8");
        Files.write(file.toPath(), bytes);
    }

    public void setRole(String player, String role, boolean flag) {
        PlayerInfo playerInfo = getPlayerInfo(player);
        switch (role) {
            case "admin":
                playerInfo.setAdmin(flag);
                break;
            case "super":
                playerInfo.setSuper(flag);
                break;
            case "judge":
                playerInfo.setJudge(flag);
                break;
        }
    }

    public boolean existsPlayer(String name) {
        return name != null && sysInfo.hasPlayer(name);
    }

    public boolean existsGame(String name) {
        return name != null && sysInfo.hasGame(name);
    }

    PlayerInfo getPlayerInfo(String name) {
        if (players.containsKey(name))
            return players.get(name);
        PlayerInfo ret = new PlayerInfo(name);
        players.put(name, ret);
        return ret;
    }

    GameInfo getGameInfo(String game) {
        if (games.containsKey(game))
            return games.get(game);
        GameInfo ret = new GameInfo(game);
        games.put(game, ret);
        return ret;
    }

    public boolean createDeck(String player, String name, String deck) {
        return getPlayerInfo(player).createDeck(name, deck);
    }

    public boolean registerPlayer(String name, String password, String email) {
        if (existsPlayer(name) || name.length() == 0)
            return false;
        players.put(name, new PlayerInfo(name, password, email));
        return true;
    }

    public void changePassword(String player, String newPassword) {
        getPlayerInfo(player).setPassword(newPassword);
    }

    public void updateProfile(String player, String email, String discordID, boolean pingDiscord) {
        getPlayerInfo(player).updateProfile(email, discordID, pingDiscord);
    }

    public boolean authenticate(String player, String password) {
        return existsPlayer(player)
                && getPlayerInfo(player).authenticate(password);
    }

    public boolean addPlayerToGame(String gameName, String playerName,
                                   String deckName) {
        PlayerInfo player = getPlayerInfo(playerName);
        String deckKey = player.getDeckKey(deckName);
        String deckContents = player.getDeck(deckKey);
        GameInfo game = getGameInfo(gameName);
        List<String> currentPlayers = Arrays.asList(game.getPlayers());
        if (!game.isOpen()) {
            return false;
        } else if (game.getRegisteredPlayerCount() == 5 && !currentPlayers.contains(playerName)) {
            return false;
        }
        game.addPlayer(playerName, deckKey, deckContents);
        player.addGame(gameName, deckKey);
        return true;
    }

    public boolean receivesTurnSummaries(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        return player.receivesTurnSummaries();
    }

    public boolean receivesPing(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        return player.receivesPing();
    }

    public void recordPlayerAccess(String playerName) {
        this.timestamps.recordPlayerAccess(playerName);
    }

    public void recordPlayerAccess(String player, String game) {
        this.timestamps.recordPlayerAccess(player, game);
    }

    public OffsetDateTime getGameTimeStamp(String game) {
        return this.timestamps.getGameTimestamp(game);
    }

    public static String getDate() {
        return OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
    }

    public OffsetDateTime getPlayerAccess(String player) {
        return this.timestamps.getPlayerAccess(player);
    }

    public OffsetDateTime getPlayerAccess(String player, String game) {
        return this.timestamps.getPlayerAccess(player, game);
    }

    public boolean isPlayerPinged(String player, String game) {
        return this.timestamps.isPlayerPinged(player, game);
    }

    /**
     * @return true if player was pinged; false if player was already pinged
     */
    public boolean pingPlayer(String playerName, String gameName) {
        if (isPlayerPinged(playerName, gameName)) {
            logger.debug("{} already pinged for {}; not pinging again", playerName, gameName);
            return false;
        }

        this.timestamps.pingPlayer(playerName, gameName);

        //Ping on Discord
        PlayerInfo player = getPlayerInfo(playerName);
        if (player != null
                && player.getDiscordID() != null
                && player.receivesDiscordPing()) {
            Unirest.post("https://discord.com/api/v{api-version}/channels/{channel-id}/messages")
                    .routeParam("api-version", DISCORD_API_VERSION)
                    .routeParam("channel-id", DISCORD_PING_CHANNEL_ID)
                    .header("Content-type", "application/json")
                    .header("Authorization", String.format("Bot %s", DISCORD_BOT_TOKEN))
                    .body(String.format("{\"content\":\"<@!%s> to %s\"}", player.getDiscordID(), gameName))
                    .asStringAsync(new Callback<String>() {
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
        return true;
    }

    public void clearPing(String player, String game) {
        this.timestamps.clearPing(player, game);
    }

    public boolean mkGame(String name) {
        if (name.length() < 2 || name.equals("admin") || name.equals("login") || name.equals("player") ||
                name.equals("card") || name.equals("showdeck") || name.equals("register") ||
                name.equals("msg") || existsGame(name) || existsPlayer(name)) {
            return false;
        }
        try {
            games.put(name, new GameInfo(name, true));
        } catch (Exception e) {
            logger.error("Error creating game", e);
            return false;
        }
        return true;
    }

    public JolGame getGame(String name) {
        return getGameInfo(name).getGame();
    }

    public String getId(String name) {
        return sysInfo.getKey(name);
    }

    public void saveGame(JolGame jolgame) {
        this.timestamps.setGameTimestamp(jolgame.getName());
        getGameInfo(jolgame.getName()).write();
    }

    public void endGame(String game) {
        getGameInfo(game).endGame();
    }

    public String[] getGames() {
        return sysInfo.getGames();
    }

    public String[] getGames(String player) {
        return getPlayerInfo(player).getGames();
    }

    public String[] getPlayers(String game) {
        return getGameInfo(game).getPlayers();
    }

    public String getEmail(String player) {
        return getPlayerInfo(player).getEmail();
    }

    public String getDiscordID(String player) {
        return getPlayerInfo(player).getDiscordID();
    }

    public boolean receivesDiscordPing(String player) {
        return getPlayerInfo(player).receivesDiscordPing();
    }

    public boolean isAdmin(String player) {
        return existsPlayer(player) && getPlayerInfo(player).isAdmin();
    }

    public String getOwner(String gameName) {
        return getGameInfo(gameName).getOwner();
    }

    public void setOwner(String game, String player) {
        getGameInfo(game).setOwner(player);
        getPlayerInfo(player).claimGame(game);
    }

    public void setGamePrivate(String game, Boolean isPrivate) {
        getGameInfo(game).setPrivate(isPrivate);
    }

    public String getDeck(String player, String name) {
        PlayerInfo info = getPlayerInfo(player);
        return info.getDeck(info.getDeckKey(name));
    }

    public String getDeckName(String game, String player) {
        PlayerInfo pinfo = getPlayerInfo(player);
        return pinfo.getGameDeckName(game);
    }

    public String getGameDeck(String game, String player) {
        GameInfo info = getGameInfo(game);
        return info.getPlayerDeck(player);
    }

    public String[] getDeckNames(String player) {
        return getPlayerInfo(player).getDeckNames();
    }

    public List<String> getPlayers() {
        return sysInfo.getPlayers();
    }

    public void invitePlayer(String gameName, String player) {
        getPlayerInfo(player).invite(gameName);
    }

    public boolean isInvited(String gameName, String player) {
        return getPlayerInfo(player).isInvited(gameName);
    }

    public boolean isOpen(String gameName) {
        return getGameInfo(gameName).isOpen();
    }

    public boolean isActive(String gameName) {
        return getGameInfo(gameName).isActive();
    }

    public boolean isFinished(String gameName) {
        return getGameInfo(gameName).isFinished();
    }

    public boolean isPrivate(String gameName) {
        return getGameInfo(gameName).isPrivate();
    }

    public void startGame(String game) {
        getGameInfo(game).startGame();
    }

    public void removeDeck(String player, String deckname) {
        logger.trace("Removing deck: {} from player: {}", deckname, player);
        getPlayerInfo(player).removeDeck(deckname);
    }

    public boolean isSuperUser(String player) {
        return existsPlayer(player) && getPlayerInfo(player).isSuperUser();
    }

    public boolean isRegistered(String gameName, String player) {
        String playerKey = sysInfo.getKey(player);
        return getGameInfo(gameName).getValue(playerKey) != null;
    }

    public boolean isJudge(String player) {
        return existsPlayer(player) && getPlayerInfo(player).isJudge();
    }

    public String getDeckId(String player, String deckName) {
        return getPlayerInfo(player).getDeckKey(deckName);
    }

    public String getPlayerId(String player) {
        return getPlayerInfo(player).id;
    }

    public void replacePlayer(String game, String oldPlayer, String newPlayer) {
        // don't replace a player with someone already in game
        if (Arrays.asList(getGameInfo(game).getPlayers()).contains(newPlayer)) {
            return;
        }
        getPlayerInfo(oldPlayer).removeGame(game);
        getPlayerInfo(newPlayer).addGame(game, "replaced");
        getGameInfo(game).replacePlayer(oldPlayer, newPlayer);
    }

    class GameInfo extends Info {
        private final String id;
        JolGame game;
        String gameName;

        private DsGame state;
        private DsTurnRecorder actions;

        GameInfo(String name) {
            this(name, sysInfo.getKey(name), false);
        }

        GameInfo(String name, String id, boolean init) {
            super(id + "/game.properties", init);
            this.id = id;
            gameName = name;
            if (!init && info.size() == 0)
                throw new IllegalArgumentException("Game " + name + " doesn't exist.");
            else if (init && info.size() > 0)
                throw new IllegalArgumentException("Game " + name + " already exists.");
        }

        GameInfo(String name, boolean create) {
            this(name, sysInfo.newGame(name), true);
            createGame(name);
        }

        private File getGameDir() {
            return new File(dir, id);
        }

        public JolGame getGame() {
            if (game == null)
                loadGame(gameName);
            return game;
        }

        private void createGame(String name) {
            try {
                getGameDir().mkdir();
                info.setProperty("state", "open");
            } catch (Exception ie) {
                logger.error("Error creating game {}", ie);
                throw new IllegalStateException("Couldn't initialize game "
                        + name);
            }
        }

        private synchronized void loadGame(String name) {
            logger.debug("Loading game {}", name);
            File gameFile = new File(getGameDir(), "game.xml");
            GameState gstate = FileUtils.loadGameState(gameFile);
            File actionsFile = new File(getGameDir(), "actions.xml");
            GameActions gactions = FileUtils.loadGameActions(actionsFile);
            state = new DsGame();
            actions = new DsTurnRecorder();
            ModelLoader.createModel(state, new StoreGame(gstate));
            ModelLoader.createRecorder(actions, new StoreTurnRecorder(gactions));
            game = new JolGame(id, state, actions);
        }

        String getHeader() {
            return "Deckserver 3.0 game file";
        }

        String getOwner() {
            return info.getProperty("owner");
        }

        void setOwner(String player) {
            info.setProperty("owner", player);
            write();
        }

        synchronized String getPlayerDeck(String player) {
            String playerKey = sysInfo.getKey(player);
            File deckFile = new File(getGameDir(), playerKey + ".deck");
            if (!deckFile.exists())
                return JolAdmin.DECK_NOT_FOUND;
            try {
                return readFile(deckFile);
            } catch (IOException ie) {
                return JolAdmin.DECK_NOT_FOUND;
            }
        }

        synchronized void addPlayer(String name, String deckKey, String deck) {
            String playerKey = sysInfo.getKey(name);
            info.setProperty(playerKey, deckKey);
            try {
                File deckFile = new File(getGameDir(), playerKey + ".deck");
                writeFile(deckFile, deck);
            } catch (IOException ie) {
                logger.error("Error creating player {}", ie);
            }
            write();
        }

        synchronized void replacePlayer(String oldPlayer, String newPlayer) {
            String oldPlayerKey = sysInfo.getKey(oldPlayer);
            String newPlayerKey = sysInfo.getKey(newPlayer);
            String deckKey = info.getProperty(oldPlayerKey);
            info.remove(oldPlayerKey);
            info.setProperty(newPlayerKey, deckKey);
            game.replacePlayer(oldPlayer, newPlayer);
            Path oldPlayerDeck = getGameDir().toPath().resolve(oldPlayerKey + ".deck");
            Path newPlayerDeck = getGameDir().toPath().resolve(newPlayerKey + ".deck");
            try {
                Files.copy(oldPlayerDeck, newPlayerDeck, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Unable to copy deck, may not exist in the first place", e);
            }
            write();
        }

        synchronized void endGame() {
            info.setProperty("state", "finished");
            // PENDING generate state html, archive all other game artifacts.
            write();
        }

        private void preStart() {
            info.setProperty("state", "closed");
            state = new DsGame();
            actions = new DsTurnRecorder();
            game = new JolGame(id, state, actions);
            game.initGame(gameName);
            regDecks();
        }

        synchronized void startGame() {
            preStart();
            getGame().startGame();
            write();
        }

        synchronized void startGame(List<String> playerSeating) {
            preStart();
            getGame().startGame(playerSeating);
            write();
        }

        private void regDecks() {
            String[] players = getPlayers();
            for (String player : players) {
                String deck = getPlayerDeck(player);
                if (deck != null) {
                    getGame().addPlayer(CardSearch.INSTANCE, player, deck);
                }
            }
        }

        public String[] getPlayers() {
            Collection<String> ps = new LinkedList<>();
            for (Object o : info.keySet()) {
                String k = (String) o;
                if (k.startsWith("player")) {
                    ps.add(sysInfo.getValue(k));
                }
            }
            return ps.toArray(new String[0]);
        }

        public long getRegisteredPlayerCount() {
            return info.entrySet().stream()
                    .filter(e -> ((String) e.getKey()).startsWith("player"))
                    .filter(e -> ((String) e.getValue()).startsWith("deck"))
                    .count();
        }

        boolean isOpen() {
            return info.getProperty("state", "closed").equals("open");
        }

        boolean isActive() {
            return info.getProperty("state", "closed").equals("closed");
        }

        boolean isFinished() {
            return info.getProperty("state", "finished").equals("finished");
        }

        protected void write() {
            dowrite();
        }

        synchronized void dowrite() {
            super.write();
            if (game != null) {
                logger.debug("Saving game {}", gameName);
                writeState(id, state, null);
                writeActions(id, actions, null);
            }
        }

        public boolean isPrivate() {
            return info.getProperty("private", "false").equals("true");
        }

        public void setPrivate(Boolean flag) {
            info.setProperty("private", flag.toString());
        }
    }

    class PlayerInfo extends Info {

        final static String DISCORD_ID_KEY = "discordID";
        final static String PING_DISCORD_KEY = "pingDiscord";

        private final String id;

        PlayerInfo(String name, String password, String email) {
            this(name, sysInfo.newPlayer(name), true);
            getPlayerDir().mkdir();
            info.setProperty("name", name);
            setPassword(password);
            setEmail(email);
        }

        PlayerInfo(String name) {
            this(name, sysInfo.getKey(name), false);
        }

        private PlayerInfo(String name, String id, boolean init) {
            super(id + "/player.properties", init);
            this.id = id;
            if (!init && info.size() == 0)
                throw new IllegalArgumentException("Player " + name
                        + " doesn't exist.");
            else if (init && info.size() > 0)
                throw new IllegalArgumentException("Player " + name
                        + " already exists.");
        }

        private File getPlayerDir() {
            return new File(dir, id);
        }

        public void setPassword(String password) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(13));
            info.setProperty("hash", hash);
            write();
        }

        boolean authenticate(String password) {
            String hash = info.getProperty("hash");
            return BCrypt.checkpw(password, hash);
        }


        void addGame(String name, String key) {
            info.setProperty(sysInfo.getKey(name), key);
            write();
        }

        void removeGame(String name) {
            info.remove(sysInfo.getKey(name));
            write();
        }

        public String[] getGames() {
            Iterator<?> i = findKeys("game").iterator();
            Collection<String> c = new ArrayList<>();
            while (i.hasNext())
                c.add(sysInfo.getValue((String) i.next()));
            return c.toArray(new String[0]);
        }

        void removeDeck(String name) {
            String key = getDeckKey(name);
            if (key != null) {
                info.remove(key);
                write();
            }
        }

        boolean createDeck(String name, String deck) {
            try {
                String key = getDeckKey(name);
                if (key == null) {
                    key = "deck" + incrementCounter("deckindex");
                    info.setProperty(key, name);
                    write();
                }
                File file = new File(getPlayerDir(), key + ".txt");
                writeFile(file, deck);
                return true;
            } catch (Exception e) {
                logger.error("Error creating deck {}", e);
                return false;
            }
        }

        String getGameDeckName(String game) {
            String id = getId(game);
            String deck = info.getProperty(id, "fubar");
            return info.getProperty(deck, "Not found");
        }

        private String getDeckKey(String name) {
            String key = getKey(name);
            if (key != null && key.startsWith("deck"))
                return key;
            return null;
        }

        private String getDeck(String deckName) {
            try {
                File file = new File(getPlayerDir(), deckName + ".txt");
                return readFile(file);
            } catch (IOException ie) {
                String msg = "Deck read Error for " + id + " and deck " + deckName;
                throw new RuntimeException(msg, ie);
            }
        }

        String[] getDeckNames() {
            return findValues("deck").toArray(new String[0]);
        }

        public boolean isAdmin() {
            String admin = info.getProperty("admin", "no");
            return !admin.equals("no");
        }

        public void setAdmin(boolean set) {
            String admin = set ? "yes" : "no";
            info.setProperty("admin", admin);
            write();
        }

        public void setSuper(boolean set) {
            String superUser = set ? "super" : "yes";
            info.setProperty("admin", superUser);
            write();
        }

        public void setJudge(boolean set) {
            String judge = set ? "yes" : "no";
            info.setProperty("judge", judge);
            write();
        }

        public String getEmail() {
            return info.getProperty("email");
        }

        public void setEmail(String email) {
            info.setProperty("email", email);
            write();
        }

        boolean isSuperUser() {
            return info.getProperty("admin", "no").equals("super");
        }

        public boolean isJudge() {
            return info.getProperty("judge", "no").equals("yes");
        }


        void claimGame(String gameName) {
            info.setProperty(sysInfo.getKey(gameName), "owner");
            write();
        }

        public boolean isOwner(String gameName) {
            return info.getProperty(sysInfo.getKey(gameName), "no").equals(
                    "owner");
        }

        void invite(String gameName) {
            info.setProperty(sysInfo.getKey(gameName), "invited");
            write();
        }

        boolean isInvited(String gameName) {
            return info.getProperty(sysInfo.getKey(gameName), "no").equals(
                    "invited");
        }

        String getHeader() {
            return "Deckserver 3.0 player information";
        }

        boolean receivesTurnSummaries() {
            return "true".equals(info.getProperty("turns", "true"));
        }

        String getDiscordID() {
            return info.getProperty(DISCORD_ID_KEY, null);
        }

        boolean receivesDiscordPing() {
            return "true".equals(info.getProperty(PING_DISCORD_KEY, "false"));
        }

        public void updateProfile(String email, String discordID, boolean pingDiscord) {
            info.setProperty("email", email);

            if (discordID != null && !"".equals(discordID)) {
                boolean isNumeric = discordID.chars().allMatch(Character::isDigit);
                if (!isNumeric)
                    throw new RuntimeException(
                            String.format(
                                    "Invalid Discord ID '%s'; must be numeric",
                                    discordID));
            }

            info.setProperty(DISCORD_ID_KEY, discordID);
            info.setProperty(PING_DISCORD_KEY, String.valueOf(pingDiscord));
            write();
        }
    }

    private class SystemInfo extends Info {
        SystemInfo() {
            super("system.properties");
        }

        String getHeader() {
            return "Deckserver 3.0 system file";
        }

        String newPlayer(String name) {
            String key = "player" + incrementCounter("playerindex");
            info.setProperty(key, name);
            write();
            return key;
        }

        String newGame(String name) {
            String key = "game" + incrementCounter("gameindex");
            info.setProperty(key, name);
            write();
            return key;
        }

        boolean hasGame(String game) {
            String key = getKey(game);
            return key != null && key.startsWith("game");
        }

        boolean hasPlayer(String player) {
            String key = getKey(player);
            return key != null && key.startsWith("player");
        }

        public String[] getGames() {
            return findValues("game").toArray(new String[0]);
        }

        public List<String> getPlayers() {
            return new ArrayList<>(findValues("player"));
        }
    }

    abstract class Info {
        final Properties info = new Properties();

        final String filename;

        Info(String filename) {
            this(filename, false);
        }

        Info(String filename, boolean ignore) {
            this.filename = dir + "/" + filename;
            load(ignore);
        }

        abstract String getHeader();

        String incrementCounter(String counter) {
            String index = info.getProperty(counter, "0");
            String num = String.valueOf(Integer.parseInt(index) + 1);
            info.setProperty(counter, num);
            write();
            return num;
        }

        Collection<String> findKeys(String pre) {
            return find(pre, true);
        }

        Collection<String> findValues(String pre) {
            return find(pre, false);
        }

        private Collection<String> find(String pre, boolean sendKey) {
            Collection<String> v = new ArrayList<>();
            for (Object o : info.keySet()) {
                String key = (String) o;
                if (key.startsWith(pre) && !key.endsWith("index")) {
                    v.add(sendKey ? key : info.getProperty(key));
                }
            }
            return v;
        }

        String getKey(String name) {
            if (!info.containsValue(name))
                return null;
            for (Map.Entry<Object, Object> objectObjectEntry : info.entrySet()) {
                if (((Map.Entry<?, ?>) objectObjectEntry).getValue().equals(name)) {
                    return (String) ((Map.Entry<?, ?>) objectObjectEntry).getKey();
                }
            }
            return null;
        }

        public String getValue(String key) {
            return info.getProperty(key);
        }

        private synchronized void load(boolean ignoreExceptions) {
            logger.debug("Reading {}", filename);
            try (FileReader in = new FileReader(filename)) {
                info.load(in);
            } catch (IOException e) {
                if (!ignoreExceptions) {
                    logger.error("Error loading {}", e);
                    throw new IllegalArgumentException("Invalid " + getHeader() + " : " + filename);
                }
            }
        }

        protected synchronized void write() {
            logger.debug("Writing {}", filename);
            try (FileWriter out = new FileWriter(filename)) {
                info.store(out, getHeader());
            } catch (IOException e) {
                logger.error("Error writing file {}", e);
            }
        }

        void setProperty(String prop, String value) {
            info.setProperty(prop, value);
        }

        public void remove(String prop) {
            info.remove(prop);
        }
    }

}
