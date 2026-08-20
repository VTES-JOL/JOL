package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.DeckPageBean;
import net.deckserver.game.enums.GameFormat;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Dedicated, envelope-free reads/writes for the React deck page — same role
 * ProfileResource/AdminPageResource play elsewhere. Deliberately separate
 * from DeckResource (still shared with legacy ds.js, still returns the
 * UpdateFactory envelope via update()) — but delegates to the exact same
 * unchanged JolAdmin methods, then returns the DeckPageBean those methods
 * already populate on PlayerModel, same bean the legacy envelope carries as
 * "callbackShowDecks".
 */
@Path("decks/player")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeckPageResource extends BaseResource {

    @GET
    public DeckPageBean getDeckPage() {
        return new DeckPageBean(JolAdmin.getPlayerModel(username()));
    }

    @POST
    @Path("new")
    public DeckPageBean newDeck() {
        JolAdmin.newDeck(username());
        return getDeckPage();
    }

    @POST
    @Path("load")
    public DeckPageBean loadDeck(LoadDeckRequest body) {
        JolAdmin.selectDeck(username(), body.deckName());
        return getDeckPage();
    }

    @POST
    public DeckPageBean saveDeck(SaveDeckRequest body) {
        JolAdmin.saveDeck(username(), body.deckName(), body.contents(), body.comment());
        return getDeckPage();
    }

    @DELETE
    @Path("{name}")
    public DeckPageBean deleteDeck(@PathParam("name") String deckName) {
        JolAdmin.deleteDeck(username(), deckName);
        return getDeckPage();
    }

    @POST
    @Path("validate")
    public DeckPageBean validate(ValidateRequest body) {
        JolAdmin.validateDeck(username(), body.contents(), GameFormat.from(body.format()));
        return getDeckPage();
    }

    public record LoadDeckRequest(String deckName) {}
    public record SaveDeckRequest(String deckName, String contents, String comment) {}
    public record ValidateRequest(String contents, String format) {}
}
