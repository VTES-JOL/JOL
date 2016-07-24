package deckserver.dwr.bean;

import deckserver.client.JolAdminFactory;
import deckserver.dwr.GameModel;
import deckserver.dwr.GameView;
import deckserver.dwr.PlayerModel;

import java.util.HashMap;
import java.util.Map;

public class NavBean {

    private static final Map<String, String> loggedIn = new HashMap<>();
    private static final Map<String, String> hasChats;
    private static final Map<String, String> loggedOut = new HashMap<>();
    private static final Map<String, String> noadmin = new HashMap<>();
    private static final Map<String, String> isadmin = new HashMap<>();
    private static final Map<String, String> suser = new HashMap<>();

    static {
        loggedOut.put("main", "Main");
        loggedIn.put("main", "Main");
        loggedIn.put("deck", "Deck Register");
        isadmin.put("admin", "Game Admin");
        suser.put("admin", "Game Admin");
        suser.put("suser", "Site Admin");
        hasChats = new HashMap<>(loggedIn);
        hasChats.put("main", "Main *");
    }

    String player, game = null, target;
    private Map<String, String> gameB = new HashMap<>(), playerB = loggedOut, adminB = noadmin;

    public NavBean(AdminBean abean, PlayerModel model) {
        player = model.getPlayer();
        target = model.getView();
        if (target.equals("game"))
            game = model.getCurrentGame();
        if (player != null) {
            playerB = model.hasChats() ? hasChats : loggedIn;
            JolAdminFactory admin = JolAdminFactory.INSTANCE;
            if (admin.isSuperUser(player)) {
                adminB = suser;
            } else if (admin.isAdmin(player)) {
                adminB = isadmin;
            }
        }
        String[] games = model.getCurrentGames();
        for (String game1 : games) {
            GameModel gmodel = abean.getGameModel(game1);
            GameView view = gmodel.getView(player);
            String current = view.isChanged() ? " *" : "";
            gameB.put("g" + game1, game1 + current);
        }
    }

    public Map<String, String> getGameButtons() {
        return gameB;
    }

    public Map<String, String> getPlayerButtons() {
        return playerB;
    }

    public Map<String, String> getAdminButtons() {
        return adminB;
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

}
