package deckserver.rich;

import nbclient.vtesmodel.JolAdminFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webclient.state.JolAdmin;

import java.util.*;


public class AdminBean {

    public static AdminBean INSTANCE = null;
    private static Logger log = LoggerFactory.getLogger(AdminBean.class);
    private static int CHAT_STORAGE = 1000;
    private static int CHAT_DISCARD = 100;

    private Map<String, GameModel> gmap = new HashMap<String, GameModel>();
    private Map<String, PlayerModel> pmap = new HashMap<String, PlayerModel>();
    private String[] who = new String[0];
    private Collection<GameModel> activeSort = new TreeSet<GameModel>();
    private List<GameModel> actives;
    private List<String> chats = new ArrayList<String>();
    private Date timestamp = new Date();
    private String[] admins = new String[0];

    public AdminBean() {
        if (INSTANCE == null)
            INSTANCE = this;
        try {
            JolAdminFactory admin = getAdmin();
            String[] games = admin.getGames();
            for (int i = 0; i < games.length; i++) {
                if (admin.isActive(games[i]) || admin.isOpen(games[i])) {
                    GameModel bean = new GameModel(games[i]);
                    gmap.put(games[i], bean);
                    activeSort.add(bean);
                }
            }
            actives = new ArrayList<GameModel>(activeSort);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public JolAdminFactory getAdmin() {
        if (JolAdminFactory.INSTANCE == null) {
            try {
                JolAdminFactory.INSTANCE =
                        new JolAdmin(System.getProperty("JOL_DATA"));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        return JolAdminFactory.INSTANCE;
    }

    /*  public JolAdminFactory getAdmin() {
        return JolAdminFactory.INSTANCE;
    } */

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
                log.info("Creating a model for " + name);
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
            for (Iterator<GameModel> i = gmap.values().iterator(); i.hasNext();
                    ) {
                i.next().resetView(player);
            }
            log.info("Removing " + player + " from admin");
            mkWho();
        }
    }

    private synchronized void mkWho() {
        Collection<String> c = new TreeSet<String>(pmap.keySet());
        who = c.toArray(new String[c.size()]);
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            if (!getAdmin().isAdmin((String) i.next())) {
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
        for (int i = 0; i < players.length; i++) {
            if (!checkViewTime(players[i]))
                players[i].chat(chat);
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
        for (Iterator<PlayerModel> i = pmap.values().iterator(); i.hasNext();
                ) {
            PlayerModel model = i.next();
            if (removed) {
                model.removeGame(name);
            } else {
                model.changeGame(name);
            }
        }
    }

    public synchronized void unEndGame(String name) {
        JolAdminFactory.INSTANCE.setGP(name, "state", "closed");
        activeSort.add(getGameModel(name));
        actives = new ArrayList<GameModel>(activeSort);
        notifyAboutGame(name);
    }

    public synchronized void endGame(String name) {
        JolAdminFactory.INSTANCE.endGame(name);
        activeSort.remove(getGameModel(name));
        actives = new ArrayList<GameModel>(activeSort);
        notifyAboutGame(name, true);
    }

    public synchronized void createGame(String name, PlayerModel player) {
        if (JolAdminFactory.INSTANCE.mkGame(name)) {
            JolAdminFactory.INSTANCE.setOwner(name, player.getPlayer());
            activeSort.add(new GameModel(name));
            actives = new ArrayList<GameModel>(activeSort);
            notifyAboutGame(name);
        }
    }

    public List<String> getChats() {
        return chats;
    }
}
