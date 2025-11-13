package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.PlayerGameActivityService;
import net.deckserver.services.RegistrationService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Getter
public class NavBean {

    private final Map<String, String> gameButtons = new HashMap<>();
    private final List<String> buttons = new ArrayList<>();
    private final String player;
    private final String target;
    private final String stamp;
    private boolean chats;
    private String game = null;

    public NavBean(PlayerModel model) {
        player = model.getPlayerName();
        target = model.getView();
        if (target.equals("game"))
            game = model.getCurrentGame();
        if (player != null) {
            chats = model.hasChats();
            boolean isAdmin = JolAdmin.isAdmin(player);
            buttons.add("active:Watch");
            buttons.add("deck:Decks");
            buttons.add("profile:Profile");
            buttons.add("lobby:Lobby");
//            buttons.add("tournament:Tournament");
            if (isAdmin) {
                buttons.add("admin:Admin");
            }
            RegistrationService.getPlayerGames(player).stream()
                    .filter(JolAdmin::isActive)
                    .filter(game -> JolAdmin.isAlive(game, player))
                    .forEach(game -> {
                        String current = PlayerGameActivityService.isCurrent(player, game) ? "" : "*";
                        gameButtons.put("g" + game, game + current);
                    });
        }
        stamp = OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
    }

}
