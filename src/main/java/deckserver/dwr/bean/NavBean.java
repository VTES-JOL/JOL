package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.GameModel;
import deckserver.dwr.GameView;
import deckserver.dwr.PlayerModel;

import java.util.HashMap;
import java.util.Map;

public class NavBean {

    private Map<String, String> adminButtons = new HashMap<>();
    private Map<String, String> gameButtons = new HashMap<>();

    private String player, target, game = null;
    private boolean chats = false;

    public NavBean(AdminBean abean, PlayerModel model) {
        JolAdmin admin = JolAdmin.getInstance();
        player = model.getPlayer();
        target = model.getView();
        if (target.equals("game"))
            game = model.getCurrentGame();
        chats = model.hasChats();
        if (player != null && admin.isAdmin(player)) {
            adminButtons.put("admin", "Game Admin");
        }
        String[] games = model.getCurrentGames();
        for (String game1 : games) {
            GameModel gmodel = abean.getGameModel(game1);
            GameView view = gmodel.getView(player);
            String current = view.isChanged() ? " *" : "";
            gameButtons.put("g" + game1, game1 + current);
        }
    }

    public Map<String, String> getGameButtons() {
        return gameButtons;
    }

    public Map<String, String> getAdminButtons() {
        return adminButtons;
    }

    public String getPlayer() {
        return player;
    }

    public String getGame() {
        return game;
    }

    public String getTarget() {
        return target;
    }

    public boolean isChats() {
        return chats;
    }
}
