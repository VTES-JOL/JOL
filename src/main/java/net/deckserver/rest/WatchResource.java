package net.deckserver.rest;

import net.deckserver.dwr.bean.AllGamesBean;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Dedicated, envelope-free read for the React "active"/"watch" page (nav
 * label "Watch") — mirrors AdminPageResource/MainResource's role. Same data
 * AllGamesCreator already serves through the legacy envelope as
 * "callbackAllGames"; ds.js keeps using that view unchanged.
 */
@Path("watch")
@Produces(MediaType.APPLICATION_JSON)
public class WatchResource extends BaseResource {

    @GET
    public AllGamesBean getAllGames() {
        return new AllGamesBean(username());
    }
}
