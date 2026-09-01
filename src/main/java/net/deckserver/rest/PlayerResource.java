package net.deckserver.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import net.deckserver.services.GameService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.game.GameSummary;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Path("/me")
public class PlayerResource {

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/activeGames")
    public List<GameSummary> activeGames() {
        String playerName = getPlayerName();
        return RegistrationService.getRegisteredGames(playerName).stream()
                .filter(GameService::isActive)
                .map(GameService::getSummary)
                .filter(summary -> summary.getPlayers().contains(playerName))
                .sorted(Comparator.comparing(GameSummary::getName))
                .toList();
    }

    @GET
    @Path("/oustedGames")
    public List<GameSummary> oustedGames() {
        String playerName = getPlayerName();
        return RegistrationService.getRegisteredGames(playerName).stream()
                .filter(GameService::isActive)
                .map(GameService::getSummary)
                .filter(summary -> !summary.getPlayers().contains(playerName))
                .sorted(Comparator.comparing(GameSummary::getName))
                .toList();
    }

    private String getPlayerName() {
        String playerName = securityContext.getUserPrincipal().getName();
        return Optional.ofNullable(playerName).orElseThrow(() -> new NotAuthorizedException("Not authorized"));
    }
}
