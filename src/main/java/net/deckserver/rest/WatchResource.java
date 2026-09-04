package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.GameSummaryBean;
import net.deckserver.services.HistoryService;
import net.deckserver.storage.json.system.GameHistory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Dedicated, envelope-free reads for the React "active"/"watch" page (nav
 * label "Watch") — mirrors AdminPageResource/MainResource's role. Same data
 * AllGamesCreator already serves through the legacy envelope as
 * "callbackAllGames"; ds.js keeps using that view unchanged.
 *
 * Split into two GETs (rather than one combined bean) because ActiveGamesTab
 * polls its list every 20s for in-game turn progress while PastGamesTab's
 * history is effectively static — bundling them meant the static list was
 * needlessly refetched on every poll.
 */
@Path("watch")
@Produces(MediaType.APPLICATION_JSON)
public class WatchResource extends BaseResource {

    @GET
    @Path("active")
    public List<GameSummaryBean> active() {
        return JolAdmin.getGameNames().stream()
                .filter(JolAdmin::isActive)
                .map(GameSummaryBean::new)
                .sorted(Comparator.comparing(GameSummaryBean::getGameName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @GET
    @Path("history")
    public List<GameHistory> history() {
        return HistoryService.getHistory().entrySet().stream()
                .sorted(Map.Entry.<OffsetDateTime, GameHistory>comparingByKey().reversed())
                .map(Map.Entry::getValue)
                .toList();
    }
}
