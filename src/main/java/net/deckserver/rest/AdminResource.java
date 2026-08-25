package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.services.HistoryService;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// setRole/deletePlayer/setMessage/getVekn/setSiteNotes/clearSiteNotes were
// ds.js-only and deleted along with ds.js/main.jsp themselves, which were
// the sole callers — the React admin page uses AdminPageResource's
// dedicated equivalents instead.
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource extends BaseResource {

    /**
     * Replaces DS.exportPastGamesAsCsv()
     */
    @GET
    @Path("export/games.csv")
    @Produces(MediaType.TEXT_PLAIN)
    public String exportPastGamesAsCsv() throws IOException {
        if (!JolAdmin.isAdmin(username())) {
            throw new ForbiddenException("Admin role required");
        }
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
}
