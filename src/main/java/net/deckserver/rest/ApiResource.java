package net.deckserver.rest;

import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.Optional;

@Path("/api")
public class ApiResource {

    private final CardSearch cardSearch = CardSearch.INSTANCE;

    public ApiResource() {
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
}
