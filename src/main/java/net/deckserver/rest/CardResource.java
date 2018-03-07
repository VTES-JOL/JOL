package net.deckserver.rest;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/card/{id}")
public class CardResource {

    @GET
    @Produces("text/html")
    public String getCard(@PathParam("id") String id) {
        JolAdmin jolAdmin = JolAdmin.getInstance();

        return Optional.ofNullable(jolAdmin.getAllCards().getCardById(id))
                .map(CardEntry::getFullText)
                .map(cardText -> Stream.of(cardText).collect(Collectors.joining("<br/>")))
                .orElse("Card not found");
    }
}
