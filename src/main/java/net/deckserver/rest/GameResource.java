package net.deckserver.rest;

import net.deckserver.services.GameService;
import net.deckserver.storage.json.game.GameSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Comparator;
import java.util.List;

@Path("/games")
@Produces("application/json")
public class GameResource {

    @Context
    private SecurityContext securityContext;

    @GET
    public List<GameSummary> getActiveGames() {
        return GameService.getActiveGames().stream()
                .map(GameService::getSummary)
                .sorted(Comparator.comparing(GameSummary::getName))
                .toList();
    }
}
