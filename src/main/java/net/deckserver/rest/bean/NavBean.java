package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.services.*;

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
    private final String stamp;
    private boolean chats;
    private boolean notificationsEnabled;
    private boolean hasSubscriptions;
    private String country = null;
    private long pendingJudgeRequests;

    public NavBean(String playerName) {
        player = playerName;
        if (player != null) {
            country = PlayerService.get(player).getCountryCode();
            notificationsEnabled = JolAdmin.getNotificationPreference(player);
            hasSubscriptions = SubscriptionService.hasSubscriptions(player);
            chats = GlobalChatService.hasUnseenChats(player);
            buttons.add("active:Watch");
            buttons.add("deck:Decks");
            buttons.add("lobby:Lobby");
            buttons.add("tournament:Tournament");
            if (JolAdmin.isJudge(player)) {
                buttons.add("judge:Judges");
                pendingJudgeRequests = JudgeService.countOpen();
            }
            if (JolAdmin.isTournamentAdmin(player)) {
                buttons.add("tournamentAdmin:Tournament Admin");
            }
            if (JolAdmin.isAdmin(player)) {
                buttons.add("admin:Admin");
            }
            RegistrationService.getPlayerGames(player).stream()
                    .filter(JolAdmin::isActive)
                    .filter(game -> JolAdmin.isAlive(game, player))
                    .forEach(game -> {
                        String current = PlayerGameActivityService.isCurrent(player, game) ? "" : "*";
                        String gameId = GameService.get(game).getId();
                        gameButtons.put("g" + gameId, game + current);
                    });
        }
        stamp = OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME);
    }

}
