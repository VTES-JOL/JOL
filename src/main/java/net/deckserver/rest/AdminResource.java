package net.deckserver.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.game.cards.RegistryStatus;
import net.deckserver.services.HistoryService;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@RolesAllowed("ADMIN")
public class AdminResource extends BaseResource {

    private static final Logger logger = LoggerFactory.getLogger(AdminResource.class);

    /**
     * Current card-database load metadata (card counts, source directory, load
     * timestamp).
     */
    @GET
    @Path("cards/status")
    public RegistryStatus cardStatus() {
        return CardRegistry.status();
    }

    /**
     * Re-parse the card CSVs from {@code jol.card.dir} and swap the in-memory
     * card database atomically — for picking up a newly released set without a
     * server restart. On a parse failure the previous data stays live.
     */
    @POST
    @Path("cards/reload")
    public RegistryStatus reloadCards() {
        logger.info("card database reload requested by {}", username());
        return CardRegistry.reload();
    }

    /**
     * Replaces DS.exportPastGamesAsCsv()
     */
    @GET
    @Path("export/games.csv")
    @Produces(MediaType.TEXT_PLAIN)
    public String exportPastGamesAsCsv() throws IOException {
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
