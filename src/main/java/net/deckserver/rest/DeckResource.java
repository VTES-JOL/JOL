package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.DeckInfoBean;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.DeckService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;

@Path("/decks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeckResource extends BaseResource {

    /** Replaces DS.filterDecks() — also used directly by the React deck page (DeckPage.tsx). */
    @GET
    public List<DeckInfoBean> filterDecks(@QueryParam("filter") @DefaultValue("") String filter) {
        String playerName = username();
        PlayerModel model = JolAdmin.getPlayerModel(playerName);
        model.setDeckFilter(filter);
        return DeckService.getPlayerDeckNames(playerName).stream()
                .map(deckName -> new DeckInfoBean(playerName, deckName))
                .filter(d -> filter.isEmpty() || d.getGameFormats().contains(filter.toUpperCase()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Replaces DS.saveDeck() — ds.js/main.jsp are themselves unreachable now
     * (MainServlet forwards every path to /react/index.html unconditionally),
     * so nothing left consumes the old UpdateFactory envelope; the React
     * deck page uses DeckPageResource's dedicated equivalents instead.
     */
    @POST
    public void saveDeck(SaveDeckRequest body) {
        JolAdmin.saveDeck(username(), body.deckName(), body.contents(), body.comment());
    }

    /** Replaces DS.deleteDeck() */
    @DELETE
    @Path("{name}")
    public void deleteDeck(@PathParam("name") String deckName) {
        JolAdmin.deleteDeck(username(), deckName);
    }

    /** Replaces DS.loadDeck() */
    @POST
    @Path("load")
    public void loadDeck(LoadDeckRequest body) {
        JolAdmin.selectDeck(username(), body.deckName());
    }

    /** Replaces DS.newDeck() */
    @POST
    @Path("new")
    public void newDeck() {
        JolAdmin.newDeck(username());
    }

    /** Replaces DS.validate() */
    @POST
    @Path("validate")
    public void validate(ValidateRequest body) {
        JolAdmin.validateDeck(username(), body.contents(), GameFormat.from(body.format()));
    }

    public record SaveDeckRequest(String deckName, String contents, String comment) {}
    public record LoadDeckRequest(String deckName) {}
    public record ValidateRequest(String contents, String format) {}
}
