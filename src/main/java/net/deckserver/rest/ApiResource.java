package net.deckserver.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.deckserver.game.json.deck.CardSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/api")
public class ApiResource {

    private Map<String, CardSummary> cardSummaryMap = new HashMap<>();

    public ApiResource() {
        File cardFile = Paths.get(System.getenv("JOL_DATA"), "cards", "cards.json").toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType cardSummaryCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, CardSummary.class);
        try {
            List<CardSummary> summaries = objectMapper.readValue(cardFile, cardSummaryCollectionType);
            cardSummaryMap = summaries.stream().collect(Collectors.toMap(CardSummary::getJolId, Function.identity()));
        } catch (IOException e) {
            System.err.println("Unable to read card data : " + e.getMessage());
        }
    }

    @GET
    @Path("/cards")
    @Produces("application/json")
    public Collection<CardSummary> cardSummaries() {
        return cardSummaryMap.values();
    }

    @GET
    @Path("/cards/{id}")
    @Produces("application/json")
    public CardSummary cardSummary(@PathParam("id") String id) {
        return cardSummaryMap.get(id);
    }
}
