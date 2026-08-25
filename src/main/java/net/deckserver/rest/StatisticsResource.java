package net.deckserver.rest;

import net.deckserver.services.HistoryService;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatisticsResource extends BaseResource {

    @POST
    @Path("/players")
    public Map<String, StatsDto> getStatsPerPlayer(StatsRequest body) {
        return getStats(body, this::generateStats);
    }

    @POST
    @Path("/decks")
    public Map<String, StatsDto> getStatsPerDeck(StatsRequest body) {
        return getStats(body, this::generateStatsPerDeck);
    }

    @POST
    @Path("/nations")
    public Map<String, StatsDto> getStatsPerNation(StatsRequest body) {
        return getStats(body, this::generateStatsPerNation);
    }

    @POST
    @Path("/performance/{playerName}/players")
    public Map<String, OpponentStats> getStatsPerOpponent(@PathParam("playerName") String playerName, StatsRequest body) {
        return getOpponents(HistoryService.getHistory().values(), playerName, body);
    }

    @POST
    @Path("/games")
    public List<GameDuration> getStatsPerGame(StatsRequest body) {
        return getGameStats(HistoryService.getHistory().values(), body);
    }

    @POST
    @Path("/jol")
    public Map<YearMonth, JolStats> getStatsJol(StatsRequest body) {
        return getJolStats(HistoryService.getHistory().values(), body);
    }
    @POST
    @Path("/performance/{playerName}/decks")
    public List<DeckMatchup> getDeckPerformance(@PathParam("playerName") String playerName, StatsRequest body) {
        return getDeckMatchs(HistoryService.getHistory().values(), playerName, body);
    }

    //Request Body for Statistics
    public record StatsRequest(int treshold, String fromDate, String toDate, boolean isTourney) {
    }

    //Get Stats for Player/Deck Statistics
    private Map<String, StatsDto> getStats(StatsRequest body, StatsGenerator generator) {
        Map<OffsetDateTime, GameHistory> history = HistoryService.getHistory();
        Map<String, Integer> gw = new HashMap<>();
        Map<String, Double> vp = new HashMap<>();
        Map<String, Double> vpMax = new HashMap<>();
        Map<String, Integer> games = new HashMap<>();
        Map<String, Set<String>> opponents = new HashMap<>();
        Map<String, Map<String, Integer>> opponentCounts = new HashMap<>();

        // Win streak
        Map<String, Integer> currentWinStreak = new HashMap<>();
        Map<String, Integer> maxWinStreak = new HashMap<>();

        history.values().stream()
                .filter(game -> !body.isTourney() || isTournamentGame(game))
                .filter(game -> isInDateRange(game, body))
                .sorted(Comparator.comparing(
                        game -> OffsetDateTime.parse(game.getEnded())
                ))
                .forEach(game ->
                        generator.generate(
                                game,
                                gw,
                                vp,
                                games,
                                vpMax,
                                opponents,
                                opponentCounts,
                                currentWinStreak,
                                maxWinStreak
                        )
                );

        Set<String> allKeys = Stream.of(games, gw, vp)
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        return allKeys.stream()
                .filter(key -> games.get(key) >= body.treshold())
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> new StatsDto(
                                String.valueOf(games.get(key)),
                                String.valueOf(gw.get(key) == null ? "-" : gw.get(key)),
                                String.valueOf(vp.get(key) == null ? "-" : vp.get(key)),
                                gw.get(key) != null
                                        ? Math.round(
                                        (Double.valueOf(gw.get(key))
                                                / Double.valueOf(games.get(key))) * 100
                                ) + "%"
                                        : "0%",
                                String.format(
                                        "%.2f",
                                        vp.get(key) / Double.valueOf(games.get(key))
                                ),
                                String.valueOf(
                                        vpMax.get(key) == null ? "-" : vpMax.get(key)
                                ),
                                String.valueOf(
                                        opponents.getOrDefault(
                                                key,
                                                Collections.emptySet()
                                        ).size()
                                ),
                                getMostPlayedOpponent(opponentCounts, key),
                                String.valueOf(
                                        maxWinStreak.getOrDefault(key, 0)
                                )
                        )
                ));
    }

    private void generateStats(
            GameHistory game,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak) {

        for (PlayerResult result : game.getResults()) {
            String name = result.getPlayerName();
            populateStats(game, name, result, gw, vp, games, vpMax, opponents, opponentCounts, currentWinStreak, maxWinStreak);
        }
    }
    private void generateStatsPerDeck(
            GameHistory game,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak
    ) {
        for (PlayerResult result : game.getResults()) {
            String name = result.getDeckName() + " / " + result.getPlayerName();
            populateStats(game, name, result, gw, vp, games, vpMax, opponents, opponentCounts, currentWinStreak, maxWinStreak);
        }
    }

    private void generateStatsPerNation(
            GameHistory game,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak
    ) {
        for (PlayerResult result : game.getResults()) {
            try {
                String name = PlayerService.get(result.getPlayerName()).getCountryCode();
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                populateStats(game, name, result, gw, vp, games, vpMax, opponents, opponentCounts, currentWinStreak, maxWinStreak);
            } catch (IllegalArgumentException e) {
                //Player not found
            }
        }
    }

    private void populateStats(
            GameHistory game,
            String name,
            PlayerResult result,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak){
        games.merge(name, 1, Integer::sum);
        vp.merge(name, result.getVictoryPoints() > 6 ? 6 : result.getVictoryPoints(), Double::sum);
        vpMax.merge(name, result.getVictoryPoints() > 6 ? 6 : result.getVictoryPoints(), Math::max);
        if (result.isGameWin()) {
            gw.merge(name, 1, Integer::sum);
            // Current streak +1
            int streak = currentWinStreak.merge(name, 1, Integer::sum);
            // Update Maximum win streak
            maxWinStreak.merge(name, streak, Math::max);
        } else {
            // Loss ends Win Streak
            currentWinStreak.put(name, 0);
        }
        // Add all other players as opponents
        String playerName = result.getPlayerName();
        game.getResults().stream()
                .map(PlayerResult::getPlayerName)
                .filter(opponent -> !opponent.equals(playerName))
                .forEach(opponent -> {

                    // Unique opponents
                    opponents
                            .computeIfAbsent(playerName, k -> new HashSet<>())
                            .add(opponent);

                    // Number of games against each opponent
                    opponentCounts
                            .computeIfAbsent(playerName, k -> new HashMap<>())
                            .merge(opponent, 1, Integer::sum);
                });
    }

    //Get Opponents Statistics
    private Map<String, OpponentStats> getOpponents(
            Collection<GameHistory> games,
            String playerName,
            StatsRequest body) {

        Map<String, OpponentStats> result = new HashMap<>();

        for (GameHistory game : games) {
            PlayerResult player = game.getResults().stream()
                    .filter(p -> playerName.equals(p.getPlayerName()))
                    .filter(data -> isInDateRange(game, body))
                    .filter(data -> !body.isTourney() || isTournamentGame(game))
                    .findFirst()
                    .orElse(null);

            if (player == null) {
                continue;
            }

            for (PlayerResult opponent : game.getResults()) {

                if (playerName.equals(opponent.getPlayerName())) {
                    continue;
                }

                String opponentName = opponent.getPlayerName();

                OpponentStats current = result.get(opponentName);

                if (current == null) {
                    current = new OpponentStats(
                            opponentName,
                            0,
                            0,
                            "",
                            0,
                            "",
                            0,
                            0
                    );
                }

                int gamesPlayed = current.games() + 1;
                int wins = current.wins() + (player.isGameWin() ? 1 : 0);
                int winOpponent = current.winOpponent() + (opponent.isGameWin() ? 1 : 0);
                int losses = current.losses() + (player.isGameWin() ? 0 : 1);
                double winRate = 0;
                double winRateOpp = 0;

                if(losses!=0) {
                    winRate = (double) wins / gamesPlayed;
                }
                if(wins+winOpponent!=0) {
                    winRateOpp = (double) wins / (wins + winOpponent);
                }

                result.put(
                        opponentName,
                        new OpponentStats(
                                opponentName,
                                gamesPlayed,
                                wins,
                                winRate != 0 ? Math.round(winRate * 100) + "%" : "0%",
                                winOpponent,
                                winRateOpp != 0 ? Math.round(winRateOpp * 100) + "%" : "0%",
                                gamesPlayed - wins - winOpponent,
                                losses
                        )
                );
            }
        }
        return result;
    }

    //Get Game Statistics
    private List<GameDuration> getGameStats(Collection<GameHistory> games, StatsRequest body) {
        return games.stream()
                .filter(game -> isInDateRange(game, body))
                .filter(game -> !body.isTourney() || isTournamentGame(game))
                .map(game -> new GameDuration(
                        game.getName(),
                        game.getResults().stream().map(PlayerResult::getPlayerName).collect(Collectors.joining(", ")),
                        getDuration(Duration.between(OffsetDateTime.parse(game.getStarted()), OffsetDateTime.parse(game.getEnded()))),
                        game.getResults().stream().anyMatch(PlayerResult::isGameWin) ? true : false,
                        game.getResults().stream().map(PlayerResult::getVP).mapToDouble(Double::parseDouble).sum()
                ))
                .toList();
    }

    //Get Jol Statistics per Month
    private Map<YearMonth, JolStats> getJolStats(
            Collection<GameHistory> games,
            StatsRequest body) {

        List<GameHistory> filteredGames = games.stream()
                .filter(game -> isInDateRange(game, body))
                .filter(game -> !body.isTourney() || isTournamentGame(game))
                .toList();

        Set<YearMonth> months = filteredGames.stream()
                .flatMap(game -> Stream.of(
                        YearMonth.from(OffsetDateTime.parse(game.getStarted())),
                        YearMonth.from(OffsetDateTime.parse(game.getEnded()))
                ))
                .collect(Collectors.toCollection(TreeSet::new));

        return months.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        month -> {

                            int started = (int) filteredGames.stream()
                                    .filter(game -> YearMonth.from(
                                            OffsetDateTime.parse(game.getStarted())
                                    ).equals(month))
                                    .count();

                            List<GameHistory> endedGames = filteredGames.stream()
                                    .filter(game -> YearMonth.from(
                                            OffsetDateTime.parse(game.getEnded())
                                    ).equals(month))
                                    .toList();

                            int ended = endedGames.size();

                            double wins = endedGames.stream()
                                    .flatMap(game -> game.getResults().stream())
                                    .filter(PlayerResult::isGameWin)
                                    .count();

                            double vp = endedGames.stream()
                                    .flatMap(game -> game.getResults().stream())
                                    .map(PlayerResult::getVictoryPoints)
                                    .filter(Objects::nonNull)
                                    .mapToDouble(v -> Math.min(v, 6))
                                    .sum();

                            double avgDurationMinutes = endedGames.stream()
                                    .mapToLong(game -> {
                                        OffsetDateTime start =
                                                OffsetDateTime.parse(game.getStarted());

                                        OffsetDateTime end =
                                                OffsetDateTime.parse(game.getEnded());

                                        return Duration.between(start, end).toMinutes();
                                    })
                                    .average()
                                    .orElse(0);

                            String bestPlayer = getBestByGw(
                                    endedGames,
                                    PlayerResult::getPlayerName
                            );

                            String bestDeck = getBestByGw(
                                    endedGames,
                                    PlayerResult::getDeckName
                            );

                            String bestNation = getBestByGw(
                                    endedGames,
                                    this::getCountryCode
                            );

                            return new JolStats(
                                    started,
                                    ended,
                                    wins,
                                    ended == 0
                                            ? "0%"
                                            : Math.round((wins / ended) * 100) + "%",
                                    vp,
                                    ended == 0
                                            ? "0"
                                            : String.format("%.2f", vp / ended),
                                    formatDuration(avgDurationMinutes),
                                    bestPlayer,
                                    bestDeck,
                                    bestNation
                            );
                        },
                        (a, b) -> a,
                        () -> new TreeMap<YearMonth, JolStats>(Comparator.reverseOrder())
                ));
    }

    private String getBestByGw(
            List<GameHistory> endedGames,
            Function<PlayerResult, String> keyExtractor) {

        Map<String, Long> gwByKey = endedGames.stream()
                .flatMap(game -> game.getResults().stream())
                .filter(PlayerResult::isGameWin)
                .filter(result -> keyExtractor.apply(result) != null)
                .collect(Collectors.groupingBy(
                        keyExtractor,
                        Collectors.counting()
                ));

        if (gwByKey.isEmpty()) {
            return "-";
        }

        long maxGw = gwByKey.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        return gwByKey.entrySet().stream()
                .filter(entry -> entry.getValue() == maxGw)
                .map(Map.Entry::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(" / "))
                + " (" + maxGw + " GW)";
    }

    private String getCountryCode(PlayerResult result) {
        try {
            return PlayerService
                    .get(result.getPlayerName())
                    .getCountryCode();
        } catch (IllegalArgumentException e) {
            return "-";
        }
    }

    private List<DeckMatchup> getDeckMatchs(Collection<GameHistory> games, String playerName, StatsRequest body) {
        return games.stream()
                .filter(game -> isInDateRange(game, body))
                .filter(game -> !body.isTourney() || isTournamentGame(game))
                .flatMap(game -> {
                    List<PlayerResult> results = game.getResults();

                    return results.stream()
                            .filter(player -> playerName.equals(player.getPlayerName()))
                            .flatMap(player -> results.stream()
                                    .filter(opponent -> player != opponent)
                                    .map(opponent ->
                                            new MatchupPair(
                                                    game,
                                                    player,
                                                    opponent
                                            )
                                    )
                            );
                })
                .collect(Collectors.groupingBy(
                        pair -> pair.player().getDeckName() + "\u0000"
                                + pair.player().getPlayerName() + "\u0000"
                                + pair.opponent().getDeckName() + "\u0000"
                                + pair.opponent().getPlayerName()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<MatchupPair> pairs = entry.getValue();

                    PlayerResult player = pairs.get(0).player();
                    PlayerResult opponent = pairs.get(0).opponent();

                    double totalVP = pairs.stream()
                            .mapToDouble(pair -> pair.player().getVictoryPoints())
                            .sum();

                    int totalWins = pairs.stream()
                            .mapToInt(pair -> pair.opponent().isGameWin() ? 1 : 0)
                            .sum();

                    double opponentTotalVP = pairs.stream()
                            .mapToDouble(pair -> pair.player().getVictoryPoints())
                            .sum();

                    long gameCount = pairs.size();

                    List<String> gameNames = pairs.stream()
                            .map(pair -> pair.game().getName())
                            .distinct()
                            .toList();

                    return new DeckMatchup(
                            player.getDeckName(),
                            gameNames.stream().collect(Collectors.joining(", ")),
                            opponent.getDeckName() + " / " + opponent.getPlayerName(),
                            gameCount,
                            totalWins,
                            String.format("%.2f", totalVP),
                            String.format("%.2f", totalVP / gameCount),
                            String.format("%.2f", opponentTotalVP),
                            String.format("%.2f", opponentTotalVP / gameCount),
                            String.format("%.2f", (totalVP - opponentTotalVP) / gameCount)
                    );
                })
                .sorted(
                        Comparator
                                .comparing(DeckMatchup::deckName,
                                        String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(DeckMatchup::opponentDeckName,
                                        String.CASE_INSENSITIVE_ORDER)
                )
                .toList();
    }

    // Utils for checking Game History Relevance
    private boolean isTournamentGame(GameHistory game) {
        return game.getName().contains("Final Table") ||
                Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(game.getName()).find();
    }

    private boolean isInDateRange(GameHistory game, StatsRequest body) {
        //without from or to value return all games
        if (body.fromDate().isEmpty() || body.toDate().isEmpty()) {
            return true;
        }
        //otherwise check if game is in date range
        LocalDate ended = OffsetDateTime.parse(game.getEnded()).toLocalDate();
        LocalDate from = LocalDate.parse(body.fromDate());
        LocalDate to = LocalDate.parse(body.toDate());
        return !ended.isBefore(from) && !ended.isAfter(to);
    }

    private String getDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return "%dd %02dh %02dm %02ds"
                .formatted(days, hours, minutes, seconds);
    }

    private String formatDuration(double minutes) {
        long totalMinutes = Math.round(minutes);

        long days = totalMinutes / (24 * 60);
        long remainingMinutes = totalMinutes % (24 * 60);

        long hours = remainingMinutes / 60;
        long mins = remainingMinutes % 60;

        if (days > 0) {
            return days + "d " + hours + "h " + mins + "m";
        }

        if (hours > 0) {
            return hours + "h " + mins + "m";
        }

        return mins + "m";
    }

    private String getMostPlayedOpponent(
            Map<String, Map<String, Integer>> opponentCounts,
            String playerName) {

        return opponentCounts
                .getOrDefault(playerName, Collections.emptyMap())
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("-");
    }

    @FunctionalInterface
    private interface StatsGenerator {
        void generate(GameHistory game,
                      Map<String, Integer> gw,
                      Map<String, Double> vp,
                      Map<String, Integer> games,
                      Map<String, Double> vpMax,
                      Map<String, Set<String>> opponents,
                      Map<String, Map<String, Integer>> opponentCounts,
                      Map<String, Integer> currentWinStreak,
                      Map<String, Integer> maxWinStreak);
    }

    //Records for returting rest call Dto's
    public record GameDuration(
            String gameName,
            String players,
            String duration,
            boolean hasGw,
            double vps
    ) {}

    public record StatsDto(
            String allGames,
            String gwCount,
            String vpCount,
            String winRate,
            String avgVp,
            String highestVp,
            String uniqueOpponents,
            String mostPlayedOpponent,
            String winStreak
    ) {
    }
    public record JolStats(
            int gamesStartedPerMonth,
            int gamesEndedPerMonth,
            double winsPerMonth,
            String winRate,
            double vpPerMonth,
            String avgVp,
            String avgDuration,
            String bestPlayer,
            String bestDeck,
            String bestNation
    ) {
    }
    public record OpponentStats(
            String opponent,
            int games,
            int wins,
            String winRate,
            int winOpponent,
            String winRateOpponent,
            int winOther,
            int losses
    ) {
    }
    private record MatchupPair(
            GameHistory game,
            PlayerResult player,
            PlayerResult opponent
    ) {}
    public record DeckMatchup(
            String deckName,
            String gameNames,
            String opponentDeckName,
            long games,
            int totalWins,
            String totalVP,
            String averageVP,
            String opponentTotalVP,
            String opponentAverageVP,
            String vpDifference
    ) {}
    public record PlayerMonthlyWins(
            String playerName,
            int gw
    ) {}
}
