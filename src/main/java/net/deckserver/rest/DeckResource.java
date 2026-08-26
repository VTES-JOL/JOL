package net.deckserver.rest;

import net.deckserver.rest.bean.DeckInfoBean;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.services.DeckService;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;

// saveDeck/deleteDeck/loadDeck/newDeck/validate were ds.js-only and deleted
// along with ds.js/main.jsp themselves, which were the sole callers — the
// React deck page uses DeckPageResource's dedicated equivalents instead.
@Path("/decks")
@Produces(MediaType.APPLICATION_JSON)
public class DeckResource extends BaseResource {

    /**
     * Replaces DS.filterDecks() — also used directly by the React deck page
     * (DeckPage.tsx). `registrable=true` additionally excludes LEGACY-format
     * decks (they can't be registered to a new game — see
     * JolAdmin.registerDeck), for the lobby/tournament deck-registration
     * dropdowns, which used to build this same filtered list themselves.
     */
    @GET
    public List<DeckInfoBean> filterDecks(
            @QueryParam("filter") @DefaultValue("") String filter,
            @QueryParam("registrable") @DefaultValue("false") boolean registrable) {
        String playerName = username();
        return DeckService.getPlayerDeckNames(playerName).stream()
                .map(deckName -> new DeckInfoBean(playerName, deckName))
                .filter(d -> filter.isEmpty() || d.getGameFormats().contains(filter.toUpperCase()))
                .filter(d -> !registrable || !d.getDeckFormat().equals(DeckFormat.LEGACY.toString()))
                .sorted(Comparator.comparing(DeckInfoBean::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
