package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.HashMap;
import java.util.Map;

public class NavBean {

    private boolean chats;
    private boolean admin;
    private boolean superUser;
    private Map<String, String> gameButtons = new HashMap<>();

    private String player, target, game = null;

    public NavBean(AdminBean abean, PlayerModel model) {
        player = model.getPlayer();
        target = model.getView();
        if (target.equals("game"))
            game = model.getCurrentGame();
        if (player != null) {
            chats = model.hasChats();
            admin = JolAdmin.getInstance().isAdmin(player);
            superUser = JolAdmin.getInstance().isSuperUser(player);
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

    public boolean isAdmin() {
        return admin;
    }

    public boolean isSuperUser() {
        return superUser;
    }
}
