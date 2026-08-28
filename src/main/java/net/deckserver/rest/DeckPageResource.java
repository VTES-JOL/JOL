package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.rest.bean.DeckPageBean;
import net.deckserver.rest.bean.DeckEdit;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.DeckImportService;
import net.deckserver.services.DeckValidityService;
import net.deckserver.storage.json.deck.DeckValidity;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Creates a deck from a confirmed import preview — the same save path a
     * normal deck edit takes (tags + per-format validity recomputed), just fed
     * canonical deck-list text built from the {cardId, count} entries.
     */
    @POST
    @Path("import")
    public DeckPageBean importDeck(ImportRequest body) {
        String name = body.name() != null && !body.name().isBlank() ? body.name() : "Imported Deck";
        String contents = DeckImportService.buildContents(
                body.entries().stream().map(e -> new DeckImportService.Entry(e.cardId(), e.count())).toList());
        return toBean(JolAdmin.saveDeck(username(), name, contents, body.comment() == null ? "" : body.comment()));
    }

    private DeckPageBean toBean(DeckEdit edit) {
        Map<String, DeckValidity> validity = edit.deckId() == null
                ? Map.of()
                : DeckValidityService.getValidity(edit.deckId()).entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        return new DeckPageBean(
                edit.deck(),
                edit.contents(),
                JolAdmin.getAvailableGameFormats(username()).stream().map(GameFormat::getLabel).toList(),
                edit.deckId(),
                validity);
    }

    public record LoadDeckRequest(String deckName) {}
    public record SaveDeckRequest(String deckName, String contents, String comment) {}
    public record ValidateRequest(String name, String contents, String format) {}
    public record ImportRequest(String name, String comment, List<ImportEntry> entries) {}
    public record ImportEntry(String cardId, int count) {}
}
