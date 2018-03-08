package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class AdminBean {

    public static final Duration TIMEOUT_INTERVAL = Duration.of(10, ChronoUnit.MINUTES);
    public static AdminBean INSTANCE = null;
    private static Logger logger = LoggerFactory.getLogger(AdminBean.class);
    private static int CHAT_STORAGE = 1000;
    private static int CHAT_DISCARD = 100;

    private Map<String, GameModel> gmap = new HashMap<>();
    private Map<String, PlayerModel> pmap = new HashMap<>();
    private List<String> who = new ArrayList<>();
    private Collection<GameModel> activeSort = new TreeSet<>();
    private List<GameModel> actives;
    private volatile List<ChatEntryBean> chats = new ArrayList<>();
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private String[] admins = new String[0];

    private File chatPersistenceFile = new File(System.getenv("JOL_DATA"), "global_chat.txt");
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

    public List<String> getWho() {
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
        who = new ArrayList<>(pmap.keySet());
    }

    public synchronized void chat(String player, String message) {
        ChatEntryBean chatEntryBean = new ChatEntryBean(player, message);
        chats.add(chatEntryBean);
        pmap.values().stream()
                .forEach(playerModel -> playerModel.chat(chatEntryBean));
    }

    private boolean checkViewTime(PlayerModel model) {
        if (model.getPlayer() == null)
            return false;
        if (model.getTimestamp().plus(TIMEOUT_INTERVAL).isBefore(timestamp)) {
            remove(model.getPlayer());
            return false;
        }
        return true;
    }

    public OffsetDateTime getTimestamp() {
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

    public synchronized List<ChatEntryBean> getChats() {
        return chats;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
