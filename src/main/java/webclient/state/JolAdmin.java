/*
 * MkState.java
 *
 * Created on February 22, 2004, 3:50 PM
 */

package webclient.state;

import cards.local.CardUtil;
import cards.model.CardSearch;
import dsclient.modelimpl.ActionHistory;
import dsclient.modelimpl.DsGame;
import dsclient.modelimpl.ModelLoader;
import javaclient.gen.GameState;
import nbclient.model.Game;
import nbclient.model.TurnRecorder;
import nbclient.model.gen.v2.GameActions;
import nbclient.modelimpl.GameImpl;
import nbclient.modelimpl.TurnImpl;
import nbclient.vtesmodel.BugDescriptor;
import nbclient.vtesmodel.JolAdminFactory;
import nbclient.vtesmodel.JolGame;
import nbclient.vtesmodel.JolGameImpl;
import util.StreamReader;

import java.io.*;
import java.util.*;

/**
 * @author Joe User
 */
public class JolAdmin extends JolAdminFactory {

    private static final Date startDate = new Date();

    private final WriterThread wt = new WriterThread();

    private final File bugdir;

    private final String dir;

    private final SystemInfo sysInfo;

    private final Map<String, GameInfo> games;

    private final Map<String, PlayerInfo> players;

    private final CardsInfo cards;

    public JolAdmin(String dir) throws Exception {
        this.dir = dir;
        this.bugdir = new File(dir, "bugs");
        games = new HashMap<String, GameInfo>();
        players = new HashMap<String, PlayerInfo>();
        cards = new CardsInfo();
        sysInfo = new SystemInfo();
    }

    /**
     * test harness *
     */
    public static void main(String[] argv) throws Throwable {
        CardUtil.setShow(true);
        System.out.println("cards");
        INSTANCE = new JolAdmin("/Users/shannon/IdeaProjects/jol-legacy/src/main/resources");
        System.out.println(INSTANCE.getBaseCards().getAllCards().getCardArray()[0].getName());
    }

    private final static String readFile(File file) throws IOException {
        return StreamReader.read(new FileInputStream(file));
    }

    private final static void writeFile(File file, String contents)
            throws IOException {
        if (!file.exists())
            file.createNewFile();
        FileWriter out = new FileWriter(file);
        out.write(contents);
        out.close();
    }

    public String dump(String value) {
        if (false) {
            System.out.println(this);
            System.out.println(games);
            System.out.println(players);
        }
        if (value == null || value.equals("") || value.equals("root"))
            return sysInfo.dump();
        String key = sysInfo.getKey(value);
        if (key.startsWith("game"))
            return getGameInfo(value).dump();
        else
            return getPlayerInfo(value).dump();
    }

    public boolean existsPlayer(String name) {
        return name != null && sysInfo.hasPlayer(name);
    }

    public boolean existsGame(String name) {
        return name != null && sysInfo.hasGame(name);
    }

    PlayerInfo getPlayerInfo(String name) {
        // assert existsPlayer
        if (players.containsKey(name))
            return players.get(name);
        PlayerInfo ret = new PlayerInfo(name);
        players.put(name, ret);
        return ret;
    }

    GameInfo getGameInfo(String game) {
        // assert existsGame
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

    public boolean addPlayerFromFile(String gameName, String playerName,
                                     String deckfile) {
        File file = new File(deckfile);
        String deck = null;
        try {
            deck = readFile(file);
        } catch (IOException ie) {
            ie.printStackTrace(System.out);
            return false;
        }
        return addPlayerInternal(gameName, playerName, file.getName(), deck);
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

    public void recordAccess(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        player.recordAccess();
    }

    public Date getLastAccess(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        return player.getLastAccess();
    }

    public boolean receivesTurnSummaries(String playerName) {
        PlayerInfo player = getPlayerInfo(playerName);
        return player.receivesTurnSummaries();
    }

    public void setReceivesTurnSummaries(String playerName, String set) {
        PlayerInfo player = getPlayerInfo(playerName);
        player.setReceivesTurnSummaries(set);
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

    public JolGame getGameFromId(String id) {
        return getGameInfo(sysInfo.getValue(id)).getGame();
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

    public boolean isSuperUser(String player) {
        return existsPlayer(player) && getPlayerInfo(player).isSuperUser();
    }

    public void setAdmin(String player, boolean set) {
        getPlayerInfo(player).setAdmin(set);
    }

    public String getOwner(String gameName) {
        return getGameInfo(gameName).getOwner();
    }

    public void setOwner(String game, String player) {
        getGameInfo(game).setOwner(player);
        getPlayerInfo(player).claimGame(game);
    }

    public void setEmail(String player, String email) {
        getPlayerInfo(player).setEmail(email);
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

    public boolean isBetaGame(String gamename) {
        return getGameInfo(gamename).isBeta();
    }

    public void startGame(String game) {
        getGameInfo(game).startGame();
    }

    public void removeDeck(String player, String deckname) {
        getPlayerInfo(player).removeDeck(deckname);
    }

    public boolean doInteractive(String playerName) {
        return getPlayerInfo(playerName).doInteractive();
    }

    public Date getGameTimeStamp(String gameName) {
        return getGameInfo(gameName).getTimeStamp();
    }

    public Collection<String> haveAccessed(String gameName) {
        return getGameInfo(gameName).getAccessed();
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

    public void setPP(String player, String prop, String value) {
        PlayerInfo info = getPlayerInfo(player);
        if (value.equals("REM")) info.remove(prop);
        else info.setProperty(prop, value);
        info.write();
    }

    public void setAP(String prop, String value) {
        if (value.equals("REM")) sysInfo.remove(prop);
        else sysInfo.setProperty(prop, value);
        sysInfo.write();
    }

    @Override
    public void replacePlayer(String game, String oldPlayer, String newPlayer) {
        setGP(game, getId(newPlayer), oldPlayer + "sub");
        setPP(newPlayer, getId(game), oldPlayer + "sub");
        final JolGame jolgame = getGame(game);
        jolgame.replacePlayer(oldPlayer, newPlayer);
        saveGame(jolgame);
    }

    public void addCardSetToGame(String game, String set) {
        getGameInfo(game).addCardSet(set);
    }

    public String[] getCardSets() {
        return cards.getCardSets();
    }

    public void addCardSet(String name, String label, String set) {
        try {
            cards.addCardSet(name, label, set);
        } catch (IOException ie) {

        }
    }

    public CardSearch getBaseCards() {
        return cards.getBaseCards();
    }

    public CardSearch getAllCards() {
        return cards.getAllCards();
    }

    public CardSearch getCardsForGame(String name) {
        String[] sets = getGameInfo(name).getCardSets();
        return cards.getCards(sets);
    }

    public BugDescriptor[] getBugs() {
        File[] files = getBugDirs();
        BugDescriptor[] bugs = new BugDescriptor[files.length];
        for (int i = 0; i < bugs.length; i++)
            bugs[i] = new BugImpl(files[i]);
        return bugs;
    }

    public BugDescriptor getBug(final String index) {
        File[] files = bugdir.listFiles(new FilenameFilter() {
            public boolean accept(File arg0, String name) {
                return name.equals(index);
            }
        });
        if (files.length == 0) return null;
        return new BugImpl(files[0]);
    }

    public void addBug(String summary, String descrip, String filer) {
        try {
            String index = StreamReader.read(new FileInputStream(new File(bugdir, "index")));
            int idx = Integer.parseInt(index) + 1;
            index = idx + "";
            BugImpl.writeContents(bugdir, "index", index);
            File newbug = new File(bugdir, index);
            BugImpl.createBug(newbug, filer, summary, descrip);
        } catch (IOException ie) {

        }
    }

    private File[] getBugDirs() {
        return bugdir.listFiles(new FileFilter() {
            public boolean accept(File arg0) {
                return arg0.isDirectory();
            }
        });
    }

    class GameInfo extends Info {
        private final String prefix;
        JolGame game;
        String gamename;
        Map<String, Date> playerAccess = new HashMap<String, Date>(8);
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

        public Date getTimeStamp() {
            String ts = info.getProperty("timestamp");
            if (ts == null)
                return new Date();
            long timestamp = Long.parseLong(ts);
            return new Date(timestamp);
        }

        public void recordAccess(String player) {
            playerAccess.put(player, new Date());
        }

        public Collection<String> getAccessed() {
            return playerAccess.keySet();
        }

        public Date getAccessed(String player) {
            Date ret = playerAccess.get(player);
            if (ret == null)
                return startDate;
            return ret;
        }

        private void createGame(String name) {
            try {
                getGameDir().mkdir();
                info.setProperty("state", "open");
                String[] sets = cards.getCoreSets();
                for (int i = 0; i < sets.length; i++) {
                    addCardSet(sets[i]);
                }
                // handled by addCardSet write();
            } catch (Exception ie) {
                ie.printStackTrace(System.err);
                throw new IllegalStateException("Couldn't initialize game "
                        + name);
            }
        }

        private void loadGame(String name) {
            System.out.println("Loading " + name);
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
                game = new JolGameImpl(state, actions);
                // System.out.println("here");
                // ModelLoader.dumpState(state,new
                // OutputStreamWriter(System.out));
            } catch (IOException ie) {
                // System.out.println("ZZZZCOULDNT");
                ie.printStackTrace(System.err);
                throw new IllegalStateException("Couldn't initialize game "
                        + name);
            }
        }

        String getHeader() {
            return "Deckserver 3.0 game file";
        }

        public String getOwner() {
            return info.getProperty("owner");
        }

        public void setOwner(String player) {
            info.setProperty("owner", player);
            write();
        }

        public String getPlayerDeck(String player) {
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

        public void addPlayer(String name, String deckKey, String deck) {
            String playerKey = sysInfo.getKey(name);
            info.setProperty(playerKey, deckKey);
            try {
                File deckFile = new File(getGameDir(), playerKey + ".deck");
                writeFile(deckFile, deck);
            } catch (IOException ie) {
                ie.printStackTrace(System.err);
            }
            write();
        }

        public void endGame() {
            info.setProperty("state", "finished");
            // PENDING generate state html, archive all other game artifacts.
            write();
        }

        public void startGame() {
            info.setProperty("state", "closed");
            state = new DsGame();
            actions = new ActionHistory();
            game = new JolGameImpl(state, actions);
            game.initGame(gamename);
            regDecks();
            getGame().startGame();
            write();
        }

        private void regDecks() {
            String[] players = getPlayers();
            for (int i = 0; i < players.length; i++) {
                String deck = getPlayerDeck(players[i]);
                if (deck != null) {
                    getGame().addPlayer(getCardsForGame(gamename), players[i],
                            deck);
                }
            }
        }

        public String[] getPlayers() {
            Collection<String> ps = new LinkedList<String>();
            for (Iterator i = info.keySet().iterator(); i.hasNext(); ) {
                String k = (String) i.next();
                if (k.startsWith("player")) {
                    ps.add(sysInfo.getValue(k));
                }
            }
            return (String[]) ps.toArray(new String[0]);
        }

        public boolean isOpen() {
            return info.getProperty("state", "closed").equals("open");
        }

        public boolean isActive() {
            return info.getProperty("state", "closed").equals("closed");
        }

        public boolean isFinished() {
            return info.getProperty("state", "finished").equals("finished");
        }

        public boolean isBeta() {
            return info.getProperty("beta", "no").equals("yes");
        }

        protected void write() {
            wt.addWrite(gamename);
            playerAccess.clear();
            info.setProperty("timestamp", (new Date()).getTime() + "");
            //dowrite();
        }

        void dowrite() {
            super.write();
            if (game != null) {
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
                    // System.err.println(state.dumpBeanNode());
                    // System.err.println(actions.dumpBeanNode());
                    ie.printStackTrace(System.err);
                } catch (NullPointerException npe) {
                    games.clear();
                    npe.printStackTrace(System.err);
                    throw new IllegalStateException("Schema2beans malfunction");
                }
            }
        }

        private void addCardSet(String name) {
            info.put("set" + name, name);
            write();
        }

        private String[] getCardSets() {
            String[] sets = (String[]) findValues("set").toArray(new String[0]);
            if (sets == null || sets.length == 0)
                return cards.getCoreSets();
            return sets;
        }
    }

    class PlayerInfo extends Info {

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

        public boolean authenticate(String password) {
            return info.getProperty("password").equals(password);
        }

        public boolean doInteractive() {
            return "yes".equals(info.getProperty("interactive", "yes"));
        }

        public void recordAccess() {
            info.setProperty("time", (new Date()).getTime() + "");
        }

        public Date getLastAccess() {
            String str = info.getProperty("time");
            long time = Long.parseLong(str);
            return new Date(time);
        }

        public void addGame(String name, String key) {
            info.setProperty(sysInfo.getKey(name), key);
            write();
        }

        public String[] getGames() {
            Iterator<?> i = findKeys("game").iterator();
            Collection<String> c = new ArrayList<String>();
            while (i.hasNext())
                c.add(sysInfo.getValue((String) i.next()));
            return (String[]) c.toArray(new String[0]);
        }

        public void removeDeck(String name) {
            String key = getDeckKey(name);
            if (key != null) {
                info.remove(key);
                write();
            }
            // PENDING leave data files around for now just in case
        }

        public boolean createDeck(String name, String deck) {
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
                e.printStackTrace(System.err);
                return false;
            }
        }

        public String getGameDeckName(String game) {
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

        public String[] getDeckNames() {
            String[] ret = (String[]) findValues("deck").toArray(new String[0]);
            return ret;
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

        public boolean isSuperUser() {
            return info.getProperty("admin", "no").equals("super");
        }

        public void claimGame(String gameName) {
            info.setProperty(sysInfo.getKey(gameName), "owner");
            write();
        }

        public boolean isOwner(String gameName) {
            return info.getProperty(sysInfo.getKey(gameName), "no").equals(
                    "owner");
        }

        public void invite(String gameName) {
            info.setProperty(sysInfo.getKey(gameName), "invited");
            write();
        }

        public boolean isInvited(String gameName) {
            return info.getProperty(sysInfo.getKey(gameName), "no").equals(
                    "invited");
        }

        String getHeader() {
            return "Deckserver 3.0 player information";
        }

        private boolean receivesTurnSummaries() {
            return "true".equals(info.getProperty("turns", "true"));
        }

        private void setReceivesTurnSummaries(String set) {
            info.setProperty("turns", set);
        }
    }

    class CardsInfo extends Info {

        private Map<String, CardSearch> map = new HashMap<String, CardSearch>();

        CardsInfo() {
            super("cards.properties");
        }

        String getHeader() {
            return "Deckserver 3.0 card set file";
        }

        public String[] getCoreSets() {
            String str = info.getProperty("standard");
            StringTokenizer tok = new StringTokenizer(str, ",");
            String[] ret = new String[tok.countTokens()];
            for (int i = 0; i < ret.length; i++)
                ret[i] = tok.nextToken();
            return ret;
        }

        public String[] getCardSets() {
            return (String[]) findValues("set").toArray(new String[0]);
        }

        public CardSearch getBaseCards() {
            if (!map.containsKey("standard")) {
                try {
                    map.put("standard", getCards(getCoreSets()));
                } catch (Exception e) {
                    // core set broken???
                }
            }
            return map.get("standard");
        }

        public CardSearch getAllCards() {
            if (!map.containsKey("allcards")) {
                try {
                    map.put("allcards", getCards(getCardSets()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return map.get("allcards");
        }

        public void addCardSet(String name, String label, String set)
                throws IOException {
            if (info.containsKey(label) || info.containsValue(name))
                throw new IllegalStateException("Set already exists");
            info.put(label, name);
            createCardSet(label, set);
        }

        private CardSearch getCards(String[] sets) {
            CardSearch[] csets = new CardSearch[sets.length];
            for (int i = 0; i < csets.length; i++) {
                try {
                    csets[i] = getCardSet(sets[i]);
                } catch (Exception e) {

                }
            }
            if (sets.length == 1)
                return csets[0];
            return CardUtil.combineSearch(csets);
        }

        private CardSearch getCardSet(String name) throws IOException {
            String label = getKey(name).substring(3);
            if (map.containsKey(label))
                return map.get(label);
            String set = readFile(new File(dir + "/cards", label + ".txt"));
            String prop = readFile(new File(dir + "/cards", label + ".prop"));
            CardSearch ret = CardUtil.createSearch(set, prop);
            map.put(label, ret);
            return ret;
        }

        private void createCardSet(String label, String set) throws IOException {
            File file = new File(dir + "/cards", label + ".txt");
            writeFile(file, set);
            // PENDING not planning to expose UI for this.
        }
    }

    class SystemInfo extends Info {
        SystemInfo() {
            super("system.properties");
        }

        String getHeader() {
            return "Deckserver 3.0 system file";
        }

        public String newPlayer(String name) {
            String key = "player" + incrementCounter("playerindex");
            info.setProperty(key, name);
            write();
            return key;
        }

        public String newGame(String name) {
            String key = "game" + incrementCounter("gameindex");
            info.setProperty(key, name);
            write();
            return key;
        }

        public boolean hasGame(String game) {
            String key = getKey(game);
            return key != null && key.startsWith("game");
        }

        public boolean hasPlayer(String player) {
            String key = getKey(player);
            return key != null && key.startsWith("player");
        }

        public String[] getGames() {
            return (String[]) findValues("game").toArray(new String[0]);
        }

        public String[] getPlayers() {
            return (String[]) findValues("player").toArray(new String[0]);
        }
    }

    abstract class Info {
        protected final Properties info = new Properties();

        protected final String filename;

        Info(String filename) {
            this(filename, false);
        }

        Info(String filename, boolean ignore) {
            this.filename = dir + "/" + filename;
            load(ignore);
        }

        abstract String getHeader();

        protected String incrementCounter(String counter) {
            String index = info.getProperty(counter, "0");
            String num = String.valueOf(Integer.parseInt(index) + 1);
            info.setProperty(counter, num);
            write();
            return num;
        }

        protected Collection<String> findKeys(String pre) {
            return find(pre, true);
        }

        protected Collection<String> findValues(String pre) {
            return find(pre, false);
        }

        private Collection<String> find(String pre, boolean sendKey) {
            Collection<String> v = new ArrayList<String>();
            for (Iterator i = info.keySet().iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                if (key.startsWith(pre) && !key.endsWith("index")) {
                    v.add(sendKey ? key : info.getProperty(key));
                }
            }
            return v;
        }

        public String getKey(String name) {
            if (!info.containsValue(name))
                return null;
            for (Iterator<?> i = info.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
                if (entry.getValue().equals(name)) {
                    return (String) entry.getKey();
                }
            }
            return null;
        }

        public String getValue(String key) {
            return info.getProperty(key);
        }

        private void load(boolean ignoreExceptions) {
            InputStream in = null;
            try {
                in = new FileInputStream(filename);
                info.load(in);
            } catch (IOException ie) {
                if (!ignoreExceptions) {
                    ie.printStackTrace(System.out);
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

        protected void write() {
            OutputStream out = null;
            try {
                out = new FileOutputStream(filename);
                info.store(out, getHeader());
            } catch (IOException ie) {
                ie.printStackTrace(System.err);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (Exception e) {

                }
            }
        }

        public String dump() {
            return info.toString();
        }

        public void setProperty(String prop, String value) {
            info.setProperty(prop, value);
        }

        public void remove(String prop) {
            info.remove(prop);
        }
    }

}
