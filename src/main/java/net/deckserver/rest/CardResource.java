package net.deckserver.rest;

import net.deckserver.rest.bean.CardDetailBean;
import net.deckserver.rest.bean.ImportPreviewBean;
import net.deckserver.services.CardSearchService;
import net.deckserver.services.DeckImportService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.List;

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
