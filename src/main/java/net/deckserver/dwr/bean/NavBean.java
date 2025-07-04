package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class NavBean {

    private final Map<String, String> gameButtons = new HashMap<>();
    private final List<String> buttons = new ArrayList<>();
    private final String player;
    private final String target;
    private final String message;
    private final String stamp;
    private boolean chats;
    private String game = null;

    public NavBean(PlayerModel model) {
        JolAdmin admin = JolAdmin.INSTANCE;
        player = model.getPlayerName();
        target = model.getView();
        if (target.equals("game"))
            game = model.getCurrentGame();
        if (player != null) {
            chats = model.hasChats();
            boolean isAdmin = admin.isAdmin(player);
            buttons.add("active:Watch");
            buttons.add("deck:Decks");
            buttons.add("profile:Profile");
            buttons.add("lobby:Lobby");
//            buttons.add("tournament:Tournament");
            if (isAdmin) {
                buttons.add("admin:Admin");
            }
        }
        admin.getGames(player).stream()
                .filter(game -> admin.isAlive(game, player))
                .forEach(game -> {
                    String current = JolAdmin.INSTANCE.isCurrent(player, game) ? "" : "*";
                    gameButtons.put("g" + game, game + current);
                });
        message = JolAdmin.INSTANCE.getMessage();
        stamp = JolAdmin.getDate();
    }

}
