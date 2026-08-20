package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.PlayerService;
import net.deckserver.services.SiteNotesService;
import net.deckserver.storage.json.system.UserSummary;

import java.util.Collections;
import java.util.List;

@Getter
public class MainBean {

    private final List<GameStatusBean> games;
    private final List<GameStatusBean> tournament;
    private final List<GameStatusBean> ousted;
    private final List<UserSummary> who;
    private final boolean loggedIn;
    private final List<ChatEntryBean> chat;
    private final String notes;

    public MainBean(PlayerModel model) {
        String playerName = model.getPlayerName();
        loggedIn = model.getPlayerName() != null;
        if (loggedIn) {
            GamesSummaryBean summary = new GamesSummaryBean(playerName);
            this.games = summary.getGames();
            this.tournament = summary.getTournament();
            this.ousted = summary.getOusted();
            chat = model.getChat();
            who = PlayerService.activeUsers();
            notes = SiteNotesService.getNotesHtml();
        } else {
            this.games = Collections.emptyList();
            this.tournament = Collections.emptyList();
            this.ousted = Collections.emptyList();
            this.chat = Collections.emptyList();
            this.who = Collections.emptyList();
            this.notes = "";
        }
    }

}
