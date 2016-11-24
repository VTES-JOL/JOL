/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package deckserver.client;

import deckserver.game.cards.CardSearch;
import deckserver.game.cards.SearchImpl;
import deckserver.game.state.DsGame;
import deckserver.game.state.Game;
import deckserver.game.state.GameImpl;
import deckserver.game.state.model.GameState;
import deckserver.game.turn.ActionHistory;
import deckserver.game.turn.TurnImpl;
import deckserver.game.turn.TurnRecorder;
import deckserver.game.turn.model.GameActions;
import deckserver.util.StreamReader;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Joe User
 */
public class JolAdmin {

    private static final Date startDate = new Date();
    private static final Logger logger = getLogger(JolAdmin.class);
    private static final JolAdmin INSTANCE = new JolAdmin(System.getProperty("jol.data"));
    private static CardSearch CARD_DATA = null;
    private final String dir;
    private final SystemInfo sysInfo;
    private final Map<String, GameInfo> games;
    private final Map<String, PlayerInfo> players;

    public JolAdmin(String dir) {
        this.dir = dir;
        games = new HashMap<>();
        players = new HashMap<>();
        sysInfo = new SystemInfo();
    }

    public static JolAdmin getInstance() {
        return INSTANCE;
    }

    private synchronized static String readFile(File file) throws IOException {
        return StreamReader.read(new FileInputStream(file));
    }

    private synchronized static void writeFile(File file, String contents)
            throws IOException {
        FileWriter out = new FileWriter(file);
        out.write(contents);
        out.flush();
        out.close();
    }

    public boolean existsPlayer(String name) {
        return name != null && sysInfo.hasPlayer(name);
    }

    public boolean existsGame(String name) {
        return name != null && sysInfo.hasGame(name);
    }

    private PlayerInfo getPlayerInfo(String name) {
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

    public boolean authenticate(String player, String password) {
        return existsPlayer(player)
                && getPlayerInfo(player).authenticate(password);
    }

    public boolean addPlayerToGame(String gameName, String playerName,
                                   String deckName) {
        PlayerInfo player = getPlayerInfo(playerName);
        String key = player.getDeckKey(deckName);
        String deck = player.getDeck(key);
        return addPlayerInternal(gameName, playerName, key, deck);
    }

    private boolean addPlayerInternal(String gameName, String playerName,
                                      String key, String deck) {
        GameInfo game = getGameInfo(gameName);
        if (!game.isOpen())
            return false;
        PlayerInfo player = getPlayerInfo(playerName);
        game.addPlayer(playerName, key, deck);
        player.addGame(gameName, key);
        return true;
    }

    public boolean receivesTurnSummaries(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        return player.receivesTurnSummaries();
    }

    public void recordAccess(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        player.recordAccess();
    }

    public Date getLastAccess(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        return player.getLastAccess();
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

    public String[] getPlayers() {
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

    public Date getGameTimeStamp(String gameName) {
        return getGameInfo(gameName).getTimeStamp();
    }

    public void recordAccess(String gameName, String playerName) {
        getGameInfo(gameName).recordAccess(playerName);
    }

    public Date getAccess(String name, String player) {
        return getGameInfo(name).getAccessed(player);
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
                String set = readFile(new File(dir + "/cards/base.txt"));
                String prop = readFile(new File(dir + "/cards/base.prop"));
                CARD_DATA = new SearchImpl(set, prop);
            } catch (IOException e) {
                throw new RuntimeException("Unable to open card files", e);
            }
        }
        return CARD_DATA;
    }

    class GameInfo extends Info {
        private final String prefix;
        JolGame game;
        String gamename;
        Map<String, Date> playerAccess = new HashMap<>(8);
        private Game state;

        private TurnRecorder actions;

        GameInfo(String name) {
            this(name, sysInfo.getKey(name), false);
        }

        GameInfo(String name, String prefix, boolean init) {
            super(prefix + "/game.properties", init);
            this.prefix = prefix;
            gamename = name;
            if (!init && info.size() == 0)
                throw new IllegalArgumentException("Game " + name
                        + " doesn't exist.");
            else if (init && info.size() > 0)
                throw new IllegalArgumentException("Game " + name
                        + " already exists.");
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
                loadGame(gamename);
            return game;
        }

        Date getTimeStamp() {
            String ts = info.getProperty("timestamp");
            if (ts == null)
                return new Date();
            long timestamp = Long.parseLong(ts);
            return new Date(timestamp);
        }

        void recordAccess(String player) {
            playerAccess.put(player, new Date());
        }

        public Collection<String> getAccessed() {
            return playerAccess.keySet();
        }

        Date getAccessed(String player) {
            Date ret = playerAccess.get(player);
            if (ret == null)
                return startDate;
            return ret;
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
            try {
                File file = new File(getGameDir(), "game.xml");
                InputStream in = new FileInputStream(file);
                GameState gstate = GameState.createGraph(in);
                in.close();
                file = new File(getGameDir(), "actions.xml");
                in = new FileInputStream(file);
                GameActions gactions = GameActions.createGraph(in);
                in.close();
                state = new DsGame();
                actions = new ActionHistory();
                ModelLoader.createModel(state, new GameImpl(gstate));
                ModelLoader.createRecorder(actions, new TurnImpl(gactions));
                game = new JolGame(state, actions);
            } catch (IOException ie) {
                logger.error("Error initializing game {}", ie);
                throw new IllegalStateException("Couldn't initialize game "
                        + name);
            }
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
                return null;
            try {
                return readFile(deckFile);
            } catch (IOException ie) {
                return null;
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

        synchronized void endGame() {
            info.setProperty("state", "finished");
            // PENDING generate state html, archive all other game artifacts.
            write();
        }

        synchronized void startGame() {
            info.setProperty("state", "closed");
            state = new DsGame();
            actions = new ActionHistory();
            game = new JolGame(state, actions);
            game.initGame(gamename);
            regDecks();
            getGame().startGame();
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
            playerAccess.clear();
            info.setProperty("timestamp", (new Date()).getTime() + "");
        }

        synchronized void dowrite() {
            super.write();
            if (game != null) {
                logger.info("Saving game {}", gamename);
                GameState gstate;
                GameActions gactions;
                try {
                    gstate = GameState.createGraph();
                    gactions = GameActions.createGraph();
                    gactions.setCounter("1");
                    gactions.setGameCounter("1");
                    GameImpl wgame = new GameImpl(gstate);
                    TurnImpl wrec = new TurnImpl(gactions);
                    ModelLoader.createModel(wgame, state);
                    ModelLoader.createRecorder(wrec, actions);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    gstate.write(bout);
                    bout.close();
                    File file = new File(getGameDir(), "game.xml");
                    OutputStream out = new FileOutputStream(file);
                    out.write(bout.toByteArray());
                    out.close();
                    bout = new ByteArrayOutputStream();
                    gactions.write(bout);
                    bout.close();
                    file = new File(getGameDir(), "actions.xml");
                    out = new FileOutputStream(file);
                    out.write(bout.toByteArray());
                    out.close();
                } catch (IOException ie) {
                    // TODO need to shut down this game at this point, so no
                    // futher data is lost.
                    logger.error("Error writing game state {}", ie);
                } catch (NullPointerException npe) {
                    games.clear();
                    logger.error("Schema2beans malfunction {}", npe);
                    throw new IllegalStateException("Schema2beans malfunction");
                }
            }
        }
    }

    private class PlayerInfo extends Info {

        private final String prefix;

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

        private PlayerInfo(String name, String prefix, boolean init) {
            super(prefix + "/player.properties", init);
            this.prefix = prefix;
            if (!init && info.size() == 0)
                throw new IllegalArgumentException("Player " + name
                        + " doesn't exist.");
            else if (init && info.size() > 0)
                throw new IllegalArgumentException("Player " + name
                        + " already exists.");
        }

        private File getPlayerDir() {
            return new File(dir, prefix);
        }

        public void setPassword(String password) {
            info.setProperty("password", password);
            write();
        }

        boolean authenticate(String password) {
            return info.getProperty("password").equals(password);
        }

        boolean doInteractive() {
            return "yes".equals(info.getProperty("interactive", "yes"));
        }

        void recordAccess() {
            info.setProperty("time", (new Date()).getTime() + "");
        }

        Date getLastAccess() {
            String str = info.getProperty("time");
            long time = Long.parseLong(str);
            return new Date(time);
        }

        void addGame(String name, String key) {
            info.setProperty(sysInfo.getKey(name), key);
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
                String msg = "Deck read Error for " + prefix + " and deck " + deckName;
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

        public String[] getPlayers() {
            return findValues("player").toArray(new String[0]);
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
            InputStream in = null;
            try {
                in = new FileInputStream(filename);
                info.load(in);
            } catch (IOException ie) {
                if (!ignoreExceptions) {
                    logger.error("Error Loading {}", ie);
                    throw new IllegalArgumentException("Invalid " + getHeader()
                            + " : " + filename);
                }
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException ie) {
                        // ignore
                    }
            }
        }

        protected synchronized void write() {
            logger.debug("Writing {}", filename);
            OutputStream out = null;
            try {
                out = new FileOutputStream(filename);
                info.store(out, getHeader());
            } catch (IOException ie) {
                logger.error("Error writing file {}", ie);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (Exception ignored) {

                }
            }
        }

        public String dump() {
            return info.toString();
        }

        void setProperty(String prop, String value) {
            info.setProperty(prop, value);
        }

        public void remove(String prop) {
            info.remove(prop);
        }
    }

}
