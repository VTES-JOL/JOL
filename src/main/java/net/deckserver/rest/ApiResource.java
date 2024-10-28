package net.deckserver.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.rest.commands.CreateGameCommand;
import net.deckserver.storage.json.cards.CardSummary;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

@Path("/api")
public class ApiResource {

    private final CardSearch cardSearch = CardSearch.INSTANCE;
    private final JolAdmin admin = JolAdmin.INSTANCE;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Context
    private HttpServletRequest servletRequest;

    @Context
    private ServletContext servletContext;

    @Context
    private SecurityContext securityContext;

    public ApiResource() {
        objectMapper.findAndRegisterModules();
    }

    @GET
    @Path("/cards")
    @Produces("application/json")
    public Collection<CardSummary> cardSummaries() {
        return cardSearch.allCards();
    }

    @GET
    @Path("/cards/{id}")
    @Produces("application/json")
    public CardSummary cardSummary(@PathParam("id") String id) {
        return cardSearch.get(id);
    }

    @GET
    @Path("/cards/{id}")
    @Produces("text/html")
    public String getCard(@PathParam("id") String id) {
        return Optional.ofNullable(cardSearch.get(id))
                .map(CardSummary::getHtmlText)
                .orElse("Card not found");
    }

    @POST
    @Path("/game")
    @Consumes("application/json")
    public void createGame(CreateGameCommand createGameCommand, @Context Principal principal) {
        admin.createGame(createGameCommand.getGameName(), createGameCommand.isPublic(), principal.getName());
    }

    @GET
    @Path("/user")
    @Produces("application/json")
    public String getUser() {
        return getPlayerName();
    }

    private String getPlayerName() {
        String playerName = securityContext.getUserPrincipal().getName();
        return Optional.ofNullable(playerName).orElseThrow(() -> new NotAuthorizedException("Not authorized"));
    }
}
