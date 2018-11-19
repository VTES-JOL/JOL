package net.deckserver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/deck/{playerId}/{deckId}")
public class DeckResource {

    @GET
    @Produces("application/json")
    public String getDeck(@PathParam("playerId") String playerId, @PathParam("deckId") String deckId) {

        try {
            java.nio.file.Path path = Paths.get(System.getenv("JOL_DATA"), playerId, deckId + ".txt");
            if (!Files.exists(path)) {
                return "Invalid deck URL";
            }
            String deckContents = new String(Files.readAllBytes(path));

            Client client = ClientBuilder.newClient();
            Response response = client.target("https://www.deckserver.net/api/decks/parse")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(deckContents, MediaType.TEXT_PLAIN));

            return response.readEntity(String.class);
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to export deck at this time";
        }

    }
}
