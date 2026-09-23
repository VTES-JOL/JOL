package net.deckserver.services;

import net.deckserver.rest.StatisticsResource;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerResult;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsService {
    
    public static List<Reaction> getPlayerReaction(StatisticsResource.StatsRequest body, List<MetricsService.CommandMetricDto> commands) {
        List<Reaction> reactions = new ArrayList<>();
        List<MetricsService.CommandMetricDto> cmds = commands.stream()
                .filter(Objects::nonNull)
                .filter(game -> isInDateRange(game.timestamp().toLocalDate(), body))
                .filter(game -> !body.isTourney() || isTournamentGame(game.gameName()))
                .toList();
        for (int i = 1; i < cmds.size(); i++) {
            MetricsService.CommandMetricDto previous = cmds.get(i - 1);
            MetricsService.CommandMetricDto current = cmds.get(i);

            if (previous.gameName().equals(current.gameName())
                    && !previous.playerName().equals(current.playerName())) {
                reactions.add(new Reaction(
                        current.gameName(),
                        previous.playerName(),
                        current.playerName(),
                        previous.timestamp().toString(),
                        current.timestamp().toString(),
                        previous.command(),
                        current.command(),
                        getDuration(Duration.between(
                                previous.timestamp(),
                                current.timestamp()
                        ))
                ));
            }
        }
        return reactions;
    }

    public static List<Reaction> getPingReaction(StatisticsResource.StatsRequest body, List<MetricsService.CommandMetricDto> commandMetricDtos) {
        List<MetricsService.CommandMetricDto> commands = MetricsService.loadCommands();
        return commands.stream()
                .filter(game -> isInDateRange(game.timestamp().toLocalDate(), body))
                .filter(game -> !body.isTourney() || isTournamentGame(game.gameName()))
                .filter(c -> c.command().startsWith("ping "))
                .flatMap(cmd -> {
                    String targetPlayer = cmd.command()
                            .substring("ping ".length())
                            .trim();
                    return commands.stream()
                            .filter(c -> c.playerName().equals(targetPlayer))
                            .filter(c -> c.gameName().equals(cmd.gameName()))
                            .filter(c -> c.timestamp().isAfter(cmd.timestamp()))
                            .min(Comparator.comparing(MetricsService.CommandMetricDto::timestamp))
                            .map(response -> new Reaction(
                                    cmd.gameName(),
                                    cmd.playerName(),
                                    targetPlayer,
                                    cmd.timestamp().toString(),
                                    response.timestamp().toString(),
                                    cmd.command(),
                                    response.command(),
                                    getDuration(Duration.between(
                                            cmd.timestamp(),
                                            response.timestamp()
                                    ))
                            ))
                            .stream();
                })
                .toList();
    }

    public static Map<String, Long> getClanPerformance(Collection<GameHistory> values, StatisticsResource.StatsRequest body) {
        return values.stream()
                //filter games in date range and tournament games
                .filter(game -> isInDateRange(game, body))
                .filter(game -> !body.isTourney() || isTournamentGame(game))
                .flatMap(gameHistory -> gameHistory.getResults().stream())
                .map(PlayerResult::getDeckName).distinct().toList().stream()
                .map(DeckService::getDeck)
                .filter(Objects::nonNull)
                .flatMap(deck -> deck.getDeck().getCrypt().getCards().stream())
                .map(card -> CardService.get(card.getName()))
                .filter(Objects::nonNull)
                .flatMap(cardSummary -> cardSummary.getClans().stream())
                .collect(Collectors.groupingBy(clan -> clan, Collectors.counting()));
    }

    

    //Get Stats for Player/Deck Statistics
    public static Map<String, StatsDto> getStats(StatisticsResource.StatsRequest body, StatsGenerator generator) {
        Map<OffsetDateTime, GameHistory> history = HistoryService.getHistory();
        Map<String, Integer> gw = new HashMap<>();
        Map<String, Double> vp = new HashMap<>();
        Map<String, Double> vpMax = new HashMap<>();
        Map<String, Long> nationPlayers = new HashMap<>();
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
                                maxWinStreak,
                                nationPlayers
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
                                ),
                                nationPlayers.get(key) == null ? "-" : String.valueOf(nationPlayers.get(key))
                        )
                ));
    }

    public static void generateStats(
            GameHistory game,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak,
            Map<String, Long> nationPlayers) {

        for (PlayerResult result : game.getResults()) {
            String name = result.getPlayerName();
            StatisticsService.populateStats(game, name, result, gw, vp, games, vpMax, opponents, opponentCounts, currentWinStreak, maxWinStreak);
        }
    }
    public static void generateStatsPerDeck(
            GameHistory game,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak,
            Map<String, Long> nationPlayers
    ) {
        for (PlayerResult result : game.getResults()) {
            String name = result.getDeckName() + " / " + result.getPlayerName();
            StatisticsService.populateStats(game, name, result, gw, vp, games, vpMax, opponents, opponentCounts, currentWinStreak, maxWinStreak);
        }
    }

    public static void generateStatsPerNation(
            GameHistory game,
            Map<String, Integer> gw,
            Map<String, Double> vp,
            Map<String, Integer> games,
            Map<String, Double> vpMax,
            Map<String, Set<String>> opponents,
            Map<String, Map<String, Integer>> opponentCounts,
            Map<String, Integer> currentWinStreak,
            Map<String, Integer> maxWinStreak,
            Map<String, Long> nationPlayers
    ) {
        for (PlayerResult result : game.getResults()) {
            try {
                String name = PlayerService.get(result.getPlayerName()).getCountryCode();
                nationPlayers.merge(name, 1L, Long::sum);
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                StatisticsService.populateStats(game, name, result, gw, vp, games, vpMax, opponents, opponentCounts, currentWinStreak, maxWinStreak);
            } catch (Exception e) {
                //Player not found
            }
        }
    }

    public static void populateStats(
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
    public static Map<String, OpponentStats> getOpponents(
            Collection<GameHistory> games,
            String playerName,
            StatisticsResource.StatsRequest body) {

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
                double oppWinRate = 0;

                if(losses!=0) {
                    winRate = (double) wins / gamesPlayed;
                }
                if(wins+winOpponent!=0) {
                    winRateOpp = (double) wins / (wins + winOpponent);
                    oppWinRate = (double) winOpponent / (wins + winOpponent);
                }

                result.put(
                        opponentName,
                        new OpponentStats(
                                opponentName,
                                gamesPlayed,
                                wins,
                                winRate != 0 ? Math.round(winRate * 100) + "%" : "0%",
                                winRateOpp != 0 ? Math.round(winRateOpp * 100) + "%" : "0%",
                                winOpponent,
                                oppWinRate!= 0 ? Math.round(oppWinRate * 100) + "%" : "0%",
                                gamesPlayed - wins - winOpponent,
                                losses
                        )
                );
            }
        }
        return result;
    }

    //Get Game Statistics
    public static List<GameDuration> getGameStats(Collection<GameHistory> games, StatisticsResource.StatsRequest body) {
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
    public static Map<YearMonth, JolStats> getJolStats(Collection<GameHistory> games, StatisticsResource.StatsRequest body) {

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
                                    result -> getCountryCode(result)
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
                        () -> new TreeMap<YearMonth, JolStats>()
                ));
    }

    public static List<Map.Entry<String, Long>> getTop3(Collection<GameHistory> games, YearMonth month, Function<PlayerResult, String> keyExtractor) {
        return games.stream()
                .filter(game -> YearMonth.from(
                        OffsetDateTime.parse(game.getEnded())
                ).equals(month))
                .flatMap(game -> game.getResults().stream())
                .filter(PlayerResult::isGameWin)
                .collect(Collectors.groupingBy(
                        keyExtractor,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .toList();
    }

    public static String getBestByGw(List<GameHistory> endedGames, Function<PlayerResult, String> keyExtractor) {

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

    public static String getCountryCode(PlayerResult result) {
        try {
            return PlayerService
                    .get(result.getPlayerName())
                    .getCountryCode();
        } catch (Exception e) {
            return "-";
        }
    }

    public static List<DeckMatchup> getDeckMatchs(Collection<GameHistory> games, String playerName, StatisticsResource.StatsRequest body) {
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

    //METRICS AND COMMANDS
    public static Map<String, List<Long>> getMetrics(
            StatisticsResource.StatsRequest body,
            List<MetricsService.PlayerMetricDto> load,
            Function<MetricsService.PlayerMetricDto, String> keyExtractor) {
        return load.stream()
                .filter(data -> {
                    if(!body.fromDate().equals("") && !body.toDate().equals("")) {
                        return data.timestamp().toLocalDate().isAfter(LocalDate.parse(body.fromDate())) &&
                                data.timestamp().toLocalDate().isBefore(LocalDate.parse(body.toDate()));
                    }
                    return true;
                })
                .filter(data -> {
                    if(body.isTourney()) {
                        return data.gameName().contains("Final Table") ||
                                Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(data.gameName()).find();
                    }
                    return true;
                })
                .collect(Collectors.groupingBy(
                        keyExtractor,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> List.of(
                                        (long) list.size(),
                                        list.stream()
                                                .filter(MetricsService.PlayerMetricDto::didChat)
                                                .count(),
                                        list.stream()
                                                .filter(MetricsService.PlayerMetricDto::didCommand)
                                                .count(),
                                        list.stream()
                                                .filter(dto -> dto.didCommand() && dto.didChat())
                                                .count(),
                                        list.stream()
                                                .filter(dto -> dto.didPing())
                                                .count()
                                )
                        )
                ));
    }

    public static Map<String, List<Long>> getCommands(
            StatisticsResource.StatsRequest body,
            List<MetricsService.CommandMetricDto> load,
            Function<MetricsService.CommandMetricDto, String> keyExtractor) {
        return load.stream()
                .filter(Objects::nonNull)
                .filter(cmd -> Objects.equals(cmd.status(),"INFO"))
                .filter(data -> {
                    if(!body.fromDate().equals("") && !body.toDate().equals("")) {
                        return data.timestamp().toLocalDate().isAfter(LocalDate.parse(body.fromDate())) &&
                                data.timestamp().toLocalDate().isBefore(LocalDate.parse(body.toDate()));
                    }
                    return true;
                })
                .filter(data -> {
                    if(body.isTourney()) {
                        return data.gameName().contains("Final Table") ||
                                Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(data.gameName()).find();
                    }
                    return true;
                })
                .collect(Collectors.groupingBy(
                        keyExtractor,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> List.of(
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("timeout"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("vp"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("choose"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("reveal"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("label"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("votes"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("random"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("flip"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("discard"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("draw"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("edge"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("play"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("influence"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("move"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("burn"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("pool"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("blood"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("contest"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("disc"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("capacity"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("unlock"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("lock"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("order"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("show"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("shuffle"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("transfer"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("rfg"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("path"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("sect"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("clan"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("open"))
                                                .count(),
                                        list.stream()
                                                .filter(cmd -> cmd.command().startsWith("ping"))
                                                .count(),
                                        list.stream().count()
                                )
                        )
                ));
    }

    // Utils for checking Game History Relevance
    public static boolean isTournamentGame(GameHistory game) {
        return isTournamentGame(game.getName());
    }
    public static boolean isTournamentGame(String gameName) {
        return gameName.contains("Final Table") ||
                Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(gameName).find();
    }

    public static boolean isInDateRange(GameHistory game, StatisticsResource.StatsRequest body) {
        return isInDateRange(OffsetDateTime.parse(game.getEnded()).toLocalDate(), body);
    }

    public static boolean isInDateRange(LocalDate timestamp, StatisticsResource.StatsRequest body) {
        //without from or to value return all games
        if (body.fromDate().isEmpty() || body.toDate().isEmpty()) {
            return true;
        }
        //otherwise check if game is in date range
        LocalDate from = LocalDate.parse(body.fromDate());
        LocalDate to = LocalDate.parse(body.toDate());
        return !timestamp.isBefore(from) && !timestamp.isAfter(to);
    }

    public static String getDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return "%dd %02dh %02dm %02ds"
                .formatted(days, hours, minutes, seconds);
    }

    public static String formatDuration(double minutes) {
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

    public static String getMostPlayedOpponent(
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

    public static long parseReactionTime(String value) {
        String[] parts = value.trim().split("\\s+");

        long days = Long.parseLong(parts[0].substring(0, parts[0].length() - 1));
        long hours = Long.parseLong(parts[1].substring(0, parts[1].length() - 1));
        long minutes = Long.parseLong(parts[2].substring(0, parts[2].length() - 1));
        long seconds = Long.parseLong(parts[3].substring(0, parts[3].length() - 1));

        return days * 86400L
                + hours * 3600L
                + minutes * 60L
                + seconds;
    }

    @FunctionalInterface
    public interface StatsGenerator {
        void generate(GameHistory game,
                      Map<String, Integer> gw,
                      Map<String, Double> vp,
                      Map<String, Integer> games,
                      Map<String, Double> vpMax,
                      Map<String, Set<String>> opponents,
                      Map<String, Map<String, Integer>> opponentCounts,
                      Map<String, Integer> currentWinStreak,
                      Map<String, Integer> maxWinStreak,
                      Map<String, Long> nationPlayers
        );
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
            String winStreak,
            String playerCount
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
            String winRateOpponent,
            int winOpponent,
            String oppWinRate,
            int winOther,
            int losses
    ) {
    }
    public static record MatchupPair(
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
    public record Reaction(
            String gameName,
            String fromPlayer,
            String targetPlayer,
            String fromTimestamp,
            String toTimestamp,
            String fromCommand,
            String toCommand,
            String reactionTime
    ) {}
}
