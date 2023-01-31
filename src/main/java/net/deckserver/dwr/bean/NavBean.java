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
    private final Map<String, String> gameButtons = new HashMap<>();

    private final String player;
    private final String target;
    private String game = null;

    public NavBean(PlayerModel model) {
        player = model.getPlayerName();
        target = model.getView();
        if (target.equals("game"))
            game = model.getCurrentGame();
        if (player != null) {
            chats = model.hasChats();
            admin = JolAdmin.getInstance().isAdmin(player);
            superUser = JolAdmin.getInstance().isSuperUser(player);
        }
        for (String game : model.getCurrentGames()) {
            GameModel gmodel = JolAdmin.getInstance().getGameModel(game);
            GameView view = gmodel.getView(player);
            String current = view.isChanged() ? " *" : "";
            gameButtons.put("g" + game, game + current);
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
