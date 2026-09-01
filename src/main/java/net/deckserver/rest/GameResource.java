package net.deckserver.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import net.deckserver.services.GameService;
import net.deckserver.storage.json.game.GameSummary;

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
