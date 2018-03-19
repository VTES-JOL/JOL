package net.deckserver.dwr.bean;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class AdminBean {

    private static final int CHAT_STORAGE = 1000;
    private static final int CHAT_DISCARD = 100;
    private static final Logger logger = LoggerFactory.getLogger(AdminBean.class);

    public static AdminBean INSTANCE = null;

    private Map<String, GameModel> gmap = new HashMap<>();
    private Map<String, PlayerModel> pmap = new HashMap<>();
    private Collection<GameModel> activeSort = new TreeSet<>();
    private List<GameModel> actives;
    private volatile List<ChatEntryBean> chats = new ArrayList<>();
    private OffsetDateTime timestamp = OffsetDateTime.now();

    // Cache of users / status
    private Cache<String, String> activeUsers = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


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
            }
        }
        return bean;
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
        PlayerModel model = getPlayerModel(player);
        if (model != null) {
            model.saveDeck();
            pmap.remove(player);
            for (GameModel gameModel : gmap.values()) {
                gameModel.resetView(player);
            }
        }
    }

    public synchronized void chat(String player, String message) {
        ChatEntryBean chatEntryBean = new ChatEntryBean(player, message);
        chats.add(chatEntryBean);
        if (chats.size() > CHAT_STORAGE) {
            chats = chats.subList(CHAT_DISCARD, CHAT_STORAGE);
        }
        pmap.values()
                .forEach(playerModel -> playerModel.chat(chatEntryBean));
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
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

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void recordAccess(PlayerModel model) {
        if (model.getPlayer() != null) {
            activeUsers.put(model.getPlayer(), model.getView());
            model.recordAccess();
        }
    }
}
