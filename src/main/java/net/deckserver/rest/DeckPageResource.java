package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.DeckPageBean;
import net.deckserver.rest.bean.DeckEdit;
import net.deckserver.game.enums.GameFormat;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Dedicated, envelope-free reads/writes for the React deck page — same role
 * ProfileResource/AdminPageResource play elsewhere. Deliberately separate
 * from DeckResource (still shared with legacy ds.js, still returns the
 * UpdateFactory envelope via update()) — but delegates to the exact same
 * unchanged JolAdmin methods, then wraps the DeckEdit those methods return
 * into a DeckPageBean, same bean the legacy envelope carries as
 * "callbackShowDecks".
 */
@Path("decks/player")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeckPageResource extends BaseResource {

    @GET
    public DeckPageBean getDeckPage() {
        return toBean(DeckEdit.EMPTY);
    }

    @POST
    @Path("new")
    public DeckPageBean newDeck() {
        return toBean(JolAdmin.newDeck(username()));
    }

    @POST
    @Path("load")
    public DeckPageBean loadDeck(LoadDeckRequest body) {
        return toBean(JolAdmin.selectDeck(username(), body.deckName()));
    }

    @POST
    public DeckPageBean saveDeck(SaveDeckRequest body) {
        return toBean(JolAdmin.saveDeck(username(), body.deckName(), body.contents(), body.comment()));
    }

    @DELETE
    @Path("{name}")
    public DeckPageBean deleteDeck(@PathParam("name") String deckName) {
        return toBean(JolAdmin.deleteDeck(username(), deckName));
    }

    @POST
    @Path("validate")
    public DeckPageBean validate(ValidateRequest body) {
        return toBean(JolAdmin.validateDeck(body.name(), body.contents(), GameFormat.from(body.format())));
    }

    private DeckPageBean toBean(DeckEdit edit) {
        return new DeckPageBean(edit.deck(), edit.contents(), JolAdmin.getAvailableGameFormats(username()).stream().map(GameFormat::getLabel).toList());
    }

    public record LoadDeckRequest(String deckName) {}
    public record SaveDeckRequest(String deckName, String contents, String comment) {}
    public record ValidateRequest(String name, String contents, String format) {}
}
