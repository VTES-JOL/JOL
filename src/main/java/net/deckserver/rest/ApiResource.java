package net.deckserver.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.deckserver.game.json.deck.CardSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Path("/api")
public class ApiResource {

    @GET
    @Path("/cards")
    @Produces("application/json")
    public List<CardSummary> cardSummaries() {
        File cardFile = Paths.get(System.getenv("JOL_DATA"), "cards", "cards.json").toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType cardSummaryCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, CardSummary.class);
        try {
            return objectMapper.readValue(cardFile, cardSummaryCollectionType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
