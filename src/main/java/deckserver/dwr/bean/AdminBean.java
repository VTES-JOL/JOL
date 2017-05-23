package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.PlayerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


public class AdminBean {

    public static AdminBean INSTANCE = null;
    private static Logger logger = LoggerFactory.getLogger(AdminBean.class);
    private static int CHAT_STORAGE = 1000;
    private static int CHAT_DISCARD = 100;

    private Map<String, GameModel> gmap = new HashMap<>();
    private Map<String, PlayerModel> pmap = new HashMap<>();
    private String[] who = new String[0];
    private Collection<GameModel> activeSort = new TreeSet<>();
    private List<GameModel> actives;
    private volatile List<String> chats = new ArrayList<>();
    private Date timestamp = new Date();
    private String[] admins = new String[0];

    private File chatPersistenceFile = new File(System.getProperty("jol.data"), "global_chat.txt");
    private String message;

    public AdminBean() {
        try {
            JolAdmin admin = JolAdmin.getInstance();
            String[] games = admin.getGames();
            for (String game : games) {
                if (admin.isActive(game) || admin.isOpen(game)) {
                    GameModel bean = new GameModel(game);
                    gmap.put(game, bean);
                    activeSort.add(bean);
                }
            }
            actives = new ArrayList<>(activeSort);
            this.chats = loadChats();
        } catch (Exception e) {
            logger.error("Error creating admin bean {}", e);
        }
    }

    /**
     * @return list of GameModels
     */
    public List<GameModel> getActiveGames() {
        return actives;
    }

    public PlayerModel getPlayerModel(String name) {
        PlayerModel bean = pmap.get(name);
        if (bean == null) {
            bean = new PlayerModel(this, name, chats);
            if (name != null) {
                logger.trace("Creating a model for " + name);
                pmap.put(name, bean);
                mkWho();
            }
        }
        return bean;
    }

    public boolean isPlayerActive(String name) {
        return pmap.containsKey(name);
    }

    public GameModel getGameModel(String name) {
        GameModel bean = gmap.get(name);
        if (bean == null) {
            bean = new GameModel(name);
            gmap.put(name, bean);
        }
        return bean;
    }

    public String[] getWho() {
        return who;
    }

    public void remove(String player) {
        PlayerModel model = getPlayerModel(player);
        if (model != null) {
            model.saveDeck();
            pmap.remove(player);
            for (GameModel gameModel : gmap.values()) {
                gameModel.resetView(player);
            }
            mkWho();
        }
    }

    private synchronized void mkWho() {
        Collection<String> c = new TreeSet<>(pmap.keySet());
        who = c.toArray(new String[c.size()]);
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            if (!JolAdmin.getInstance().isAdmin((String) i.next())) {
                i.remove();
            }
        }
        admins = c.toArray(new String[c.size()]);
    }

    public synchronized void chat(String chat) {
        chats.add(chat);
        if (chats.size() > CHAT_STORAGE) {
            chats = chats.subList(CHAT_DISCARD, CHAT_STORAGE);
        }
        PlayerModel[] players = pmap.values().toArray(new PlayerModel[0]);
        timestamp = new Date();
        for (PlayerModel player : players) {
            if (!checkViewTime(player))
                player.chat(chat);
        }
    }

    private boolean checkViewTime(PlayerModel model) {
        if (model.getPlayer() == null)
            return true;
        if (timestamp.getTime() - model.getTimestamp() >
                GameModel.TIMEOUT_INTERVAL) {
            remove(model.getPlayer());
            return true;
        }
        return false;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String[] getAdmins() {
        return admins;
    }

    public void notifyAboutGame(String name) {
        notifyAboutGame(name, false);
    }

    private void notifyAboutGame(String name, boolean removed) {
        for (PlayerModel model : pmap.values()) {
            if (removed) {
                model.removeGame(name);
            } else {
                model.changeGame(name);
            }
        }
    }

    public synchronized void unEndGame(String name) {
        JolAdmin.getInstance().setGP(name, "state", "closed");
        activeSort.add(getGameModel(name));
        actives = new ArrayList<>(activeSort);
        notifyAboutGame(name);
    }

    public synchronized void endGame(String name) {
        JolAdmin.getInstance().endGame(name);
        activeSort.remove(getGameModel(name));
        actives = new ArrayList<>(activeSort);
        notifyAboutGame(name, true);
    }

    public synchronized void createGame(String name, PlayerModel player) {
        logger.trace("Creating game {} for player {}", name, player.getPlayer());
        if (JolAdmin.getInstance().mkGame(name)) {
            JolAdmin.getInstance().setOwner(name, player.getPlayer());
            activeSort.add(new GameModel(name));
            actives = new ArrayList<>(activeSort);
            notifyAboutGame(name);
        }
    }

    public synchronized List<String> getChats() {
        return chats;
    }

    private synchronized List<String> loadChats() {
        try {
            List<String> chatLines = Files.readAllLines(chatPersistenceFile.toPath());
            logger.debug("Loaded chat state: {} loaded", chatLines.size());
            return chatLines;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public synchronized void persistChats() {
        try {
            Files.write(chatPersistenceFile.toPath(), this.chats);
            logger.debug("Saved chat state: {} stored", this.chats.size());
        } catch (IOException e) {
            logger.error("Error persisting chat");
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
