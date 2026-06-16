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
import java.util.Map;

@Path("/decks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeckResource extends BaseResource {

    /** Replaces DS.filterDecks() */
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

    /** Replaces DS.saveDeck() */
    @POST
    public Map<String, Object> saveDeck(SaveDeckRequest body) {
        String playerName = username();
        JolAdmin.saveDeck(playerName, body.deckName(), body.contents(), body.comment());
        return update(playerName);
    }

    /** Replaces DS.deleteDeck() */
    @DELETE
    @Path("{name}")
    public Map<String, Object> deleteDeck(@PathParam("name") String deckName) {
        String playerName = username();
        JolAdmin.deleteDeck(playerName, deckName);
        return update(playerName);
    }

    /** Replaces DS.loadDeck() */
    @POST
    @Path("load")
    public Map<String, Object> loadDeck(LoadDeckRequest body) {
        String playerName = username();
        JolAdmin.selectDeck(playerName, body.deckName());
        return update(playerName);
    }

    /** Replaces DS.newDeck() */
    @POST
    @Path("new")
    public Map<String, Object> newDeck() {
        String playerName = username();
        JolAdmin.newDeck(playerName);
        return update(playerName);
    }

    /** Replaces DS.validate() */
    @POST
    @Path("validate")
    public Map<String, Object> validate(ValidateRequest body) {
        String playerName = username();
        JolAdmin.validateDeck(playerName, body.contents(), GameFormat.from(body.format()));
        return update(playerName);
    }

    public record SaveDeckRequest(String deckName, String contents, String comment) {}
    public record LoadDeckRequest(String deckName) {}
    public record ValidateRequest(String contents, String format) {}
}
