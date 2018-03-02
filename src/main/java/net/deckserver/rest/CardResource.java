package net.deckserver.rest;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/card/{id}")
public class CardResource {

    private CacheControl cacheControl;

    public CardResource() {
        cacheControl = new CacheControl();
        cacheControl.setMaxAge(86400);
        cacheControl.setPrivate(false);
        cacheControl.setNoTransform(false);
    }

    @GET
    @Produces("text/html")
    public Response getCard(@PathParam("id") String id) {
        JolAdmin jolAdmin = JolAdmin.getInstance();
        Optional<CardEntry> result = Optional.ofNullable(jolAdmin.getAllCards().getCardById(id));

        String html = result
                .map(CardEntry::getFullText)
                .map(cardText -> Stream.of(cardText).collect(Collectors.joining("<br/>")))
                .orElse("Card not found");

        EntityTag entityTag = new EntityTag(Integer.toString(html.hashCode()));

        return Response.ok(html).cacheControl(cacheControl).tag(entityTag).build();
    }
}
