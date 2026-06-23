package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.system.PlayerInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import net.deckserver.services.HistoryService;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource extends BaseResource {

    /** Replaces DS.setRole() */
    @PUT
    @Path("player/{name}/role")
    public Map<String, Object> setRole(@PathParam("name") String player, SetRoleRequest body) {
        String playerName = username();
        PlayerInfo target = PlayerService.get(player);
        if (JolAdmin.isAdmin(playerName)) {
            JolAdmin.setRole(target, PlayerRole.valueOf(body.role()), body.value());
        }
        return update(playerName);
    }

    /** Replaces DS.deletePlayer() */
    @DELETE
    @Path("player/{name}")
    public Map<String, Object> deletePlayer(@PathParam("name") String targetPlayer) {
        String player = username();
        if (JolAdmin.isAdmin(player)) {
            JolAdmin.deletePLayer(targetPlayer);
        }
        return update(player);
    }

    /** Replaces DS.setMessage() */
    @POST
    @Path("message")
    public Map<String, Object> setMessage(MessageRequest body) {
        String playerName = username();
        // message setting is a no-op in current impl but retained for compatibility
        return update(playerName);
    }

    /** Replaces DS.getVekn() */
    @GET
    @Path("player/{name}/vekn")
    public String getVekn(@PathParam("name") String playerName) {
        username(); // auth check
        return PlayerService.get(playerName).getVeknId();
    }

    /** Replaces DS.exportPastGamesAsCsv() */
    @GET
    @Path("export/games.csv")
    @Produces(MediaType.TEXT_PLAIN)
    public String exportPastGamesAsCsv() throws IOException {
        username(); // auth check
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Game", "Started", "Ended", "Player", "Deck", "GW", "VP")
                .setQuoteMode(QuoteMode.ALL)
                .build();
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        DateTimeFormatter csvDateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm");
        Map<OffsetDateTime, GameHistory> history = HistoryService.getHistory();
        if (history.isEmpty()) {
            return "NO DATA AVAILABLE";
        }
        for (GameHistory game : history.values()) {
            for (PlayerResult player : game.getResults()) {
                String startTime = OffsetDateTime.parse(game.getStarted(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).format(csvDateTimeFormatter);
                String endTime = OffsetDateTime.parse(game.getEnded(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).format(csvDateTimeFormatter);
                printer.printRecord(game.getName(), startTime, endTime, player.getPlayerName(), player.getDeckName(),
                        player.isGameWin() ? "GW" : "", String.valueOf(player.getVP()).replace(".", ","));
            }
        }
        return writer.toString();
    }

    public record SetRoleRequest(String role, boolean value) {}
    public record MessageRequest(String message) {}
}
