package net.deckserver.rest;

import net.deckserver.services.*;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    @POST
    @Path("/players")
    public Map<String, StatisticsService.StatsDto> getStatsPerPlayer(StatsRequest body) {
        return StatisticsService.getStats(body, StatisticsService::generateStats);
    }

    @POST
    @Path("/decks")
    public Map<String, StatisticsService.StatsDto> getStatsPerDeck(StatsRequest body) {
        return StatisticsService.getStats(body, StatisticsService::generateStatsPerDeck);
    }

    @POST
    @Path("/nations")
    public Map<String, StatisticsService.StatsDto> getStatsPerNation(StatsRequest body) {
        return StatisticsService.getStats(body, StatisticsService::generateStatsPerNation);
    }

    @POST
    @Path("/performance/{playerName}/players")
    public Map<String, StatisticsService.OpponentStats> getStatsPerOpponent(@PathParam("playerName") String playerName, StatsRequest body) {
        return StatisticsService.getOpponents(HistoryService.getHistory().values(), playerName, body);
    }

    @POST
    @Path("/games")
    public List<StatisticsService.GameDuration> getStatsPerGame(StatsRequest body) {
        return StatisticsService.getGameStats(HistoryService.getHistory().values(), body);
    }

    @POST
    @Path("/jol/month")
    public Map<YearMonth, StatisticsService.JolStats> getStatsJolMonth(StatsRequest body) {
        return StatisticsService.getJolStats(HistoryService.getHistory().values(), body);
    }
    @POST
    @Path("/jol/clans")
    public Map<String, Long> getStatsJolClans(StatsRequest body) {
        return StatisticsService.getClanPerformance(HistoryService.getHistory().values(), body);
    }

    @GET
    @Path("/jol/kpis/{playerName}")
    public MetricsService.JolFacts getStatsJolKpis(@PathParam("playerName") String playerName) {
        return MetricsService.getStats(playerName);
    }

    @POST
    @Path("/performance/{playerName}/decks")
    public List<StatisticsService.DeckMatchup> getDeckPerformance(@PathParam("playerName") String playerName, StatsRequest body) {
        return StatisticsService.getDeckMatchs(HistoryService.getHistory().values(), playerName, body);
    }

    @POST
    @Path("/metrics/player")
    public Map<String, List<Long>> getMetricsPlayer(StatsRequest body) {
        return StatisticsService.getMetrics(
                body,
                MetricsService.loadMetrics(),
                MetricsService.PlayerMetricDto::playerName
        );
    }

    @POST
    @Path("/metrics/game")
    public Map<String, List<Long>> getMetricsGame(StatsRequest body) {
        return StatisticsService.getMetrics(
                body,
                MetricsService.loadMetrics(),
                MetricsService.PlayerMetricDto::gameName
        );
    }

    @POST
    @Path("/commands/player")
    public Map<String, List<Long>> getCommandsPlayer(StatsRequest body) {
        return StatisticsService.getCommands(
                body,
                MetricsService.loadCommands(),
                MetricsService.CommandMetricDto::playerName
        );
    }

    @POST
    @Path("/commands/game")
    public Map<String, List<Long>> getCommandsGame(StatsRequest body) {
        return StatisticsService.getCommands(
                body,
                MetricsService.loadCommands(),
                MetricsService.CommandMetricDto::gameName
        );
    }

    @POST
    @Path("/commands/reaction/ping")
    public Map<String, List<StatisticsService.Reaction>> getPingReaction(StatsRequest body) {
        return StatisticsService.getPingReaction(body, MetricsService.loadCommands()).stream()
                .collect(Collectors.groupingBy(
                        StatisticsService.Reaction::targetPlayer));
    }

    @POST
    @Path("/reaction/{playerName}")
    public List<StatisticsService.Reaction> getCommandsReaction(@PathParam("playerName") String playerName, StatsRequest body) {
        return StatisticsService.getPlayerReaction(body, MetricsService.loadCommands()).stream()
                .filter(reaction -> Objects.equals(reaction.targetPlayer(), playerName)).toList();
    }
    @POST
    @Path("/reaction/avg")
    public Map<String, String> getCommandsReactionAvg(StatsRequest body) {
        return StatisticsService.getPlayerReaction(body, MetricsService.loadCommands()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        StatisticsService.Reaction::fromPlayer,
                        Collectors.collectingAndThen(
                                Collectors.averagingLong(
                                        r -> StatisticsService.parseReactionTime(r.reactionTime())
                                ),
                                avg -> StatisticsService.formatDuration(avg)
                        )
                ));
    }

    //Request Body for Statistics
    public record StatsRequest(int treshold, String fromDate, String toDate, boolean isTourney) {
    }
}
