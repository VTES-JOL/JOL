package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.services.PlayerService;
import net.deckserver.services.SiteNotesService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @GET
    @Path("stats")
    public Map<String, List<String>> getStatsPerPlayer() {
        Map<OffsetDateTime, GameHistory> history = HistoryService.getHistory();
        Map<String, Integer> gw = new HashMap<>();
        Map<String, Double> vp = new HashMap<>();
        Map<String, Integer> games = new HashMap<>();
        for (GameHistory game : history.values()) {
            for (PlayerResult player : game.getResults()) {
                //GW
                if(gw.containsKey(player.getPlayerName()) && player.isGameWin()){
                    gw.put(player.getPlayerName(), gw.get(player.getPlayerName()) + 1);
                } else if( !gw.containsKey(player.getPlayerName()) && player.isGameWin()) {
                    gw.put(player.getPlayerName(), 1);
                }
                //VP
                if(vp.containsKey(player.getPlayerName())){
                    vp.put(player.getPlayerName(), vp.get(player.getPlayerName()) + player.getVictoryPoints());
                } else if( !vp.containsKey(player.getPlayerName())) {
                    vp.put(player.getPlayerName(), player.getVictoryPoints());
                }
                //Games
                if(games.containsKey(player.getPlayerName())){
                    games.put(player.getPlayerName(), games.get(player.getPlayerName()) + 1);
                } else if( !games.containsKey(player.getPlayerName())) {
                    games.put(player.getPlayerName(), 1);
                }
            }
        }

        Set<String> allKeys = Stream.of(games, gw, vp)
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        return allKeys.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> Stream.of(
                                        String.valueOf(games.get(key)),
                                        String.valueOf(gw.get(key) == null ? "-" :gw.get(key)),
                                        String.valueOf(vp.get(key) == null ? "-" :vp.get(key)),
                                        gw.get(key) != null ? Math.round((Double.valueOf(gw.get(key)) / Double.valueOf(games.get(key))) * 100) +"%" : "0%",
                                        String.valueOf(Math.floor(vp.get(key) / Double.valueOf(games.get(key)) * 100) / 100))
                                .toList()));
    }

    /** Sets the global site notes shown on the main page. */
    @PUT
    @Path("site-notes")
    public Map<String, Object> setSiteNotes(SiteNotesRequest body) {
        String playerName = username();
        if (JolAdmin.isAdmin(playerName)) {
            SiteNotesService.setNotes(body.notes());
        }
        return update(playerName);
    }

    /** Clears the global site notes. */
    @DELETE
    @Path("site-notes")
    public Map<String, Object> clearSiteNotes() {
        String playerName = username();
        if (JolAdmin.isAdmin(playerName)) {
            SiteNotesService.clear();
        }
        return update(playerName);
    }

    public record SetRoleRequest(String role, boolean value) {}
    public record MessageRequest(String message) {}
    public record SiteNotesRequest(String notes) {}
}
