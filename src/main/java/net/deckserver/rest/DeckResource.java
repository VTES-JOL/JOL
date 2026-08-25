package net.deckserver.rest;

import net.deckserver.dwr.bean.DeckInfoBean;
import net.deckserver.services.DeckService;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;

// saveDeck/deleteDeck/loadDeck/newDeck/validate were ds.js-only and deleted
// along with ds.js/main.jsp themselves, which were the sole callers — the
// React deck page uses DeckPageResource's dedicated equivalents instead.
@Path("/decks")
@Produces(MediaType.APPLICATION_JSON)
public class DeckResource extends BaseResource {

    /** Replaces DS.filterDecks() — also used directly by the React deck page (DeckPage.tsx). */
    @GET
    public List<DeckInfoBean> filterDecks(@QueryParam("filter") @DefaultValue("") String filter) {
        String playerName = username();
        return DeckService.getPlayerDeckNames(playerName).stream()
                .map(deckName -> new DeckInfoBean(playerName, deckName))
                .filter(d -> filter.isEmpty() || d.getGameFormats().contains(filter.toUpperCase()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
