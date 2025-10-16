package net.deckserver.rest;

import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.system.UserSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Path("/stats")
public class SystemResource {

    @Path("/activeUsers")
    @GET
    public List<UserSummary> online() {
        return PlayerService.activeUsers();
    }
}
