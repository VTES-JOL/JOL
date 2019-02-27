/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package net.deckserver.dwr.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.time.OffsetDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Joe User
 */
public class JolAdmin {

    public static final String DECK_NOT_FOUND = "Deck not found";
    private static final Logger logger = getLogger(JolAdmin.class);
    private static final JolAdmin INSTANCE = new JolAdmin(System.getenv("JOL_DATA"));
    private static CardSearch CARD_DATA = null;
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

    public void updateProfile(String player, String email, boolean receivePing, boolean receiveSummary) {
        getPlayerInfo(player).updateProfile(email, receivePing, receiveSummary);
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
        if (!game.isOpen() || game.getRegisteredPlayerCount() == 5)
            return false;
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

    public void pingPlayer(String player, String game) {
        this.timestamps.pingPlayer(player, game);
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

    public boolean doInteractive(String playerName) {
        return getPlayerInfo(playerName).doInteractive();
    }

    public void setGP(String gamename, String prop, String value) {
        GameInfo info = getGameInfo(gamename);
        if (value.equals("REM")) info.remove(prop);
        else info.setProperty(prop, value);
        info.write();
    }

    public CardSearch getAllCards() {
        if (CARD_DATA == null) {
            logger.info("Loading Card Data");
            try {
                Path cardsPath = Paths.get(dir, "cards", "cards.json");

                CARD_DATA = new CardSearch(cardsPath);
            } catch (IOException e) {
                throw new RuntimeException("Unable to open card files", e);
            }
        }
        return CARD_DATA;
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

    public String parseMessage(String message) {
        return getAllCards().parseText(message);
    }

    class GameInfo extends Info {
        private final String prefix;
        JolGame game;
        String gameName;

        private DsGame state;
        private DsTurnRecorder actions;

        GameInfo(String name) {
            this(name, sysInfo.getKey(name), false);
        }

        GameInfo(String name, String prefix, boolean init) {
            super(prefix + "/game.properties", init);
            this.prefix = prefix;
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
            return new File(dir, prefix);
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
            game = new JolGame(state, actions);
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
            write();
        }

        synchronized void endGame() {
            info.setProperty("state", "finished");
            // PENDING generate state html, archive all other game artifacts.
            write();
        }

        synchronized void startGame() {
            info.setProperty("state", "closed");
            state = new DsGame();
            actions = new DsTurnRecorder();
            game = new JolGame(state, actions);
            game.initGame(gameName);
            regDecks();
            getGame().startGame();
            write();
        }

        synchronized void startGame(List<String> playerSeating) {
            info.setProperty("state", "closed");
            state = new DsGame();
            actions = new DsTurnRecorder();
            game = new JolGame(state, actions);
            game.initGame(gameName);
            regDecks();
            getGame().startGame(playerSeating);
            write();
        }

        private void regDecks() {
            String[] players = getPlayers();
            for (String player : players) {
                String deck = getPlayerDeck(player);
                if (deck != null) {
                    getGame().addPlayer(getAllCards(), player, deck);
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
                GameState gstate = new GameState();
                GameActions gactions = new GameActions();
                gactions.setCounter("1");
                gactions.setGameCounter("1");
                StoreGame wgame = new StoreGame(gstate);
                StoreTurnRecorder wrec = new StoreTurnRecorder(gactions);
                ModelLoader.createModel(wgame, state);
                ModelLoader.createRecorder(wrec, actions);
                File gameFile = new File(getGameDir(), "game.xml");
                FileUtils.saveGameState(gstate, gameFile);
                File actionsFile = new File(getGameDir(), "actions.xml");
                FileUtils.saveGameActions(gactions, actionsFile);
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

        boolean doInteractive() {
            return "yes".equals(info.getProperty("interactive", "yes"));
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

        boolean receivesPing() {
            return "true".equals(info.getProperty("pings", "true"));
        }

        public void updateProfile(String email, boolean receivePing, boolean receiveSummary) {
            info.setProperty("email", email);
            info.setProperty("pings", String.valueOf(receivePing));
            info.setProperty("turns", String.valueOf(receiveSummary));
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
