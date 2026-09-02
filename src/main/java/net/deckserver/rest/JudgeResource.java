package net.deckserver.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.deckserver.rest.bean.JudgeQueueBean;
import net.deckserver.services.JudgeService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.game.JudgeRequestData;

import java.util.List;
import java.util.Set;

/**
 * The judges page: the outstanding "call a judge" queue and the ruling history.
 * Resolution itself happens in-game (see {@code GameStateResource}) — this
 * resource is read-only; rows link the judge to {@code /jol/game/{gameId}}.
 */
@Path("/judge")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("JUDGE")
public class JudgeResource extends BaseResource {

    @GET
    @Path("requests")
    public JudgeQueueBean requests() {
        String judge = username();
        List<JudgeQueueBean.Entry> open = JudgeService.listOpen().stream()
                .map(d -> JudgeQueueBean.Entry.of(d, canRule(judge, d)))
                .toList();
        List<JudgeQueueBean.Entry> history = JudgeService.listResolved().stream()
                .map(d -> JudgeQueueBean.Entry.of(d, false))
                .toList();
        return new JudgeQueueBean(open, history);
    }

    /**
     * A judge may rule when they are not a seated player in that game — and,
     * until tournament→judge assignment exists, only for non-tournament games.
     */
    private boolean canRule(String judge, JudgeRequestData d) {
        if (d.isTournament() || d.getGameId() == null) {
            return false;
        }
        Set<String> players = RegistrationService.getPlayers(d.getGameName());
        return players == null || !players.contains(judge);
    }
}
