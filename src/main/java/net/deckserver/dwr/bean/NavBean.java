package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class NavBean {

    private boolean chats;
    private final Map<String, String> gameButtons = new HashMap<>();
    private final List<String> buttons = new ArrayList<>();

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
            boolean admin = JolAdmin.getInstance().isAdmin(player);
            buttons.add("active:Watch");
            buttons.add("deck:Decks");
            buttons.add("profile:Profile");
            buttons.add("lobby:Lobby");
            buttons.add("tournament:Tournament");
            if (admin) {
                buttons.add("admin:Admin");
            }
        }
        buttons.add("help:Help");
        for (String game : model.getCurrentGames()) {
            GameModel gmodel = JolAdmin.getInstance().getGameModel(game);
            GameView view = gmodel.getView(player);
            String current = view.isChanged() ? " *" : "";
            gameButtons.put("g" + game, game + current);
        }
    }

}
