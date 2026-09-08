package net.deckserver.rest;

import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.rest.bean.CardBean;
import net.deckserver.rest.bean.CardDetailBean;
import net.deckserver.rest.bean.ImportPreviewBean;
import net.deckserver.services.CardSearchService;
import net.deckserver.services.DeckImportService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Card lookups for the React deck editor — autocomplete suggestions and batch
 * detail fetch, both projected from {@link net.deckserver.game.cards.CardRegistry}
 * via {@link CardSearchService}. Authenticated (deck editing only).
 */
@Path("cards")
@Produces(MediaType.APPLICATION_JSON)
public class CardResource extends BaseResource {

    @GET
    @Path("autocomplete")
    public List<CardDetailBean> autocomplete(@QueryParam("q") @DefaultValue("") String query) {
        return CardSearchService.autocomplete(query);
    }

    @GET
    @Path("details")
    public List<CardDetailBean> details(@QueryParam("ids") @DefaultValue("") String ids) {
        if (ids.isBlank()) {
            return List.of();
        }
        return CardSearchService.findDetailsByIds(Arrays.asList(ids.split(",")));
    }

    // Card reference data is immutable at runtime (only POST /admin/cards/reload
    // changes it), so the text-mode lookups below carry a long cache lifetime.
    private static final String IMMUTABLE_CACHE = "public, max-age=86400, immutable";

    /**
     * Full structured card record for the text-only card mode (brief 6c) —
     * name, type line, clan / disciplines, capacity / cost, card text, etc.
     * Not on the per-game snapshot path. 404 if the id is unknown.
     */
    @GET
    @Path("{id}")
    public Response card(@PathParam("id") String id) {
        Card card = CardRegistry.findById(id);
        if (card == null) {
            throw new NotFoundException("No card with id " + id);
        }
        return Response.ok(CardBean.of(card)).header("Cache-Control", IMMUTABLE_CACHE).build();
    }

    /**
     * Batch form of {@link #card} — {@code GET /cards?ids=a,b,c}. Unknown ids
     * are silently dropped; order follows the query string. Empty / missing
     * {@code ids} returns {@code []}.
     */
    @GET
    public Response cards(@QueryParam("ids") @DefaultValue("") String ids) {
        List<CardBean> result = ids.isBlank() ? List.of() : Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(CardRegistry::findById)
                .filter(Objects::nonNull)
                .map(CardBean::of)
                .toList();
        return Response.ok(result).header("Cache-Control", IMMUTABLE_CACHE).build();
    }

    /**
     * Previews a pasted deck list — auto-detects KRCG JSON vs plain JOL text,
     * resolves every card against the database, and returns matches + errors
     * so the import modal can show what would be created.
     */
    @POST
    @Path("preview")
    @Consumes(MediaType.TEXT_PLAIN)
    public ImportPreviewBean preview(String text) {
        return DeckImportService.preview(text);
    }
}
