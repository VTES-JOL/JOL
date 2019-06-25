package net.deckserver.rest;

import net.deckserver.dwr.model.ChatParser;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Optional;

@Path("/card/{id}")
public class CardResource {

    @GET
    @Produces("text/html")
    public String getCard(@PathParam("id") String id) {
        JolAdmin jolAdmin = JolAdmin.getInstance();

        return Optional.ofNullable(CardSearch.INSTANCE.getCardById(id))
                .map(CardEntry::getFullText)
                .map(cardText -> String.join("<br/>", cardText))
                .map(ChatParser::parseText)
                .orElse("Card not found");
    }
}
