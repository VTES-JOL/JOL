package net.deckserver.services;

import net.deckserver.JolAdmin;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameSummary;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerInfo;
import net.deckserver.storage.json.system.PlayerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetricsService {

    private static final Path METRICS_PERSISTENCE_PATH = DataPaths.path("metrics");
    private static final Path COMMAND_PERSISTENCE_PATH = DataPaths.path("commands");

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    public static JolFacts getStats(String forPlayer) {
        Set<String> allGames = JolAdmin.getGameNames();
        Set<String> allActiveGames = allGames.stream().filter(JolAdmin::isActive).collect(Collectors.toSet());
        //Number of all active Games
        long activeGames = allActiveGames.stream().count();
        //Number of all active tournament Games
        long tournamentGames = allActiveGames.stream()
                .filter(JolAdmin::isTournament)
                .count();
        Collection<GameHistory> histories = HistoryService.getGames();
        //Active Games Last Month
        long activeLastMonth = getAllActiveGamesLastMonth(histories,YearMonth.now().minusMonths(1));
        long activeBeforeLast = getAllActiveGamesLastMonth(histories,YearMonth.now().minusMonths(2));
        long activeThreeMonths = getAllActiveGamesLastMonth(histories,YearMonth.now().minusMonths(3));
        String activeChangeMonth = activeLastMonth == 0
                ? (activeGames == 0 ? "0.0%" : "100.0%")
                : String.format(
                "%+.1f%%",
                ((double) activeGames - activeLastMonth)
                        / activeLastMonth * 100
        );
        String activeChangeLast = activeBeforeLast == 0
                ? (activeLastMonth == 0 ? "0.0%" : "100.0%")
                : String.format(
                "%+.1f%%",
                ((double) activeLastMonth - activeBeforeLast)
                        / activeBeforeLast * 100
        );
        String activeChangeBefore = activeThreeMonths == 0
                ? (activeBeforeLast == 0 ? "0.0%" : "100.0%")
                : String.format(
                "%+.1f%%",
                ((double) activeBeforeLast - activeThreeMonths)
                        / activeThreeMonths * 100
        );
        //Active Tournament Games Last Month
        long activeTourLastMonth = getAllActiveGamesLastMonth(histories.stream()
                .filter(game -> isTournamentGame(game)).toList(), YearMonth.now().minusMonths(1));
        long activeTourBefore = getAllActiveGamesLastMonth(histories.stream()
                .filter(game -> isTournamentGame(game)).toList(), YearMonth.now().minusMonths(2));
        long activeTourBeforeThree = getAllActiveGamesLastMonth(histories.stream()
                .filter(game -> isTournamentGame(game)).toList(), YearMonth.now().minusMonths(3));
        String activeTourChangeMonth = activeTourLastMonth == 0
                ? (tournamentGames == 0 ? "0.0%" : "100.0%")
                : String.format(
                "%+.1f%%",
                ((double) tournamentGames - activeTourLastMonth)
                        / activeTourLastMonth * 100
        );
        String activeTourChangeLast = activeTourBefore == 0
                ? (activeTourLastMonth == 0 ? "0.0%" : "100.0%")
                : String.format(
                "%+.1f%%",
                ((double) tournamentGames - activeTourBefore)
                        / activeTourBefore * 100
        );
        String activeTourChangeBefore = activeTourBeforeThree == 0
                ? (activeTourBefore == 0 ? "0.0%" : "100.0%")
                : String.format(
                "%+.1f%%",
                ((double) activeTourBefore - activeTourBeforeThree)
                        / activeTourBeforeThree * 100
        );
        //Number of all past Games
        long pastGames = histories.stream()
                .count();
        //Number of all past Tournament Games
        long pastTournament = histories.stream()
                .filter(game -> isTournamentGame(game))
                .count();
        List<ExtendedDeck> allDecks = DeckService.getDecks();
        //Number of all decks ever created
        long deckCount = allDecks.size();

        //Summary of all Active Games
        List<GameSummary> allGamesSummary = allActiveGames.stream()
                .map(gameName -> {
                    try {
                        return GameService.getSummary(gameName);
                    } catch (Exception ex) {
                        return null;/*no game found*/
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //Player and Count of Active Games
        Map<String, Long> gamesByPlayer = allGamesSummary.stream()
                .flatMap(game -> game.getPlayers().stream())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
        Map<String, Long> pastByPlayer = getAllPastGamesLastMonth(histories);

        //Player and Count of Ousted Games
        Map<String, Long> oustedByPlayer = allGamesSummary.stream()
                .flatMap(game -> game.getPlayers().stream())
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        playerName -> allGamesSummary.stream()
                                .filter(game -> !game.getPlayers().contains(playerName))
                                .count()
                ));
        //Player and Count of Active Tournament Games
        Map<String, Long> tournamentsByPlayer = allGamesSummary.stream()
                .filter(game -> game.getName().contains("Final Table") ||
                        Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(game.getName()).find())
                .flatMap(game -> game.getPlayers().stream())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
        Map<String, Long> pastTournamentByPlayer = getAllPastGamesLastMonth(histories.stream().filter(game -> isTournamentGame(game)).toList());

        //Player and Count of Decks
        Map<String, Long> decksByPlayer = allDecks.stream()
                .map(deck -> deck.getDeck().getAuthor())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        List<PlayerInfo> allPlayers = PlayerService.getPlayers().stream()
                .map(player -> PlayerService.get(player)).collect(Collectors.toList());
        //Nation and Count of Players
        Map<String, Long> nationsByPlayer = allPlayers.stream().map(PlayerInfo::getCountryCode)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        //Activity
        List<MetricsService.PlayerMetricDto> metrics = loadMetrics();
        //Months
        MonthlyActivityDto currentMonth = calculateMonthlyActivity(metrics, YearMonth.now());
        MonthlyActivityDto lastMonth = calculateMonthlyActivity(metrics, YearMonth.now().minusMonths(1));
        MonthlyActivityDto monthBeforeLast = calculateMonthlyActivity(metrics, YearMonth.now().minusMonths(2));
        //Overall
        OverallOverviewDto overallOverviewDto = calculateOverallOverview(metrics);
        //Days and Hours
        Map<String, Long> peakActivityDays = getTopActivity(metrics, m -> m.timestamp().toLocalDate().toString(), 10);
        Map<String, Long> peakActivityHours = getTopActivity(metrics, m -> String.valueOf(m.timestamp().getHour()), 10);
        //PLAYER
        PlayerActivityOverviewDto playerActivity = getPlayerActivity(metrics, forPlayer);
        List<ChatCommandRatioDto> chatCommandRatioDto = getTopChatCommandRatios(metrics);

        //Top 3
        List<Map.Entry<String, Long>> top3PlayersCurrent = StatisticsService.getTop3(histories, YearMonth.now(), PlayerResult::getPlayerName);
        List<Map.Entry<String, Long>> top3PlayersLast = StatisticsService.getTop3(histories, YearMonth.now().minusMonths(1), PlayerResult::getPlayerName);
        List<Map.Entry<String, Long>> top3PlayersBeforeLast = StatisticsService.getTop3(histories, YearMonth.now().minusMonths(2), PlayerResult::getPlayerName);
        List<Map.Entry<String, Long>> top3DecksCurrent = StatisticsService.getTop3(histories, YearMonth.now(), PlayerResult::getDeckName);
        List<Map.Entry<String, Long>> top3DecksLast = StatisticsService.getTop3(histories, YearMonth.now().minusMonths(1), PlayerResult::getDeckName);
        List<Map.Entry<String, Long>> top3DecksBeforeLast = StatisticsService.getTop3(histories, YearMonth.now().minusMonths(2), PlayerResult::getDeckName);
        List<Map.Entry<String, Long>> top3NationsCurrent = StatisticsService.getTop3(histories, YearMonth.now(), result -> StatisticsService.getCountryCode(result));
        List<Map.Entry<String, Long>> top3NationsLast = StatisticsService.getTop3(histories, YearMonth.now().minusMonths(1), result -> StatisticsService.getCountryCode(result));
        List<Map.Entry<String, Long>> top3PNationsBeforeLast = StatisticsService.getTop3(histories, YearMonth.now().minusMonths(2), result -> StatisticsService.getCountryCode(result));

        return new JolFacts(
                List.of(activeGames, activeLastMonth, activeBeforeLast),
                List.of(activeChangeMonth, activeChangeLast, activeChangeBefore),
                List.of(tournamentGames, activeTourLastMonth, activeTourBefore),
                List.of(activeTourChangeMonth, activeTourChangeLast, activeTourChangeBefore),
                pastGames,
                pastTournament,
                deckCount,
                sort(gamesByPlayer),
                sort(pastByPlayer),
                sort(tournamentsByPlayer),
                sort(pastTournamentByPlayer),
                sort(oustedByPlayer),
                sort(decksByPlayer),
                sort(nationsByPlayer),
                List.of(currentMonth, lastMonth, monthBeforeLast),
                overallOverviewDto,
                peakActivityDays,
                peakActivityHours,
                playerActivity,
                chatCommandRatioDto,
                List.of(top3PlayersCurrent, top3PlayersLast, top3PlayersBeforeLast),
                List.of(top3DecksCurrent, top3DecksLast, top3DecksBeforeLast),
                List.of(top3NationsCurrent, top3NationsLast, top3PNationsBeforeLast));
    }


    public static List<PlayerMetricDto> loadMetrics() {
        logger.info("Loading Metrics from File System");
        try {
            return Files.walk(METRICS_PERSISTENCE_PATH)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .flatMap(path -> readMetrics(path).stream())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<CommandMetricDto> loadCommands() {
        logger.info("Loading Commands from File System");
        try {
            return Files.walk(COMMAND_PERSISTENCE_PATH)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .flatMap(path -> readCommands(path).stream())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<PlayerMetricDto> readMetrics(Path file) {
        try {
            return Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(line -> {
                        String[] values = splitCsvLine(line);

                        int year = Integer.parseInt(values[0]);
                        int month = Integer.parseInt(values[1]);
                        int day = Integer.parseInt(values[2]);
                        int hour = Integer.parseInt(values[3]);

                        OffsetDateTime timestamp = OffsetDateTime.of(
                                year,
                                month,
                                day,
                                hour,
                                0,
                                0,
                                0,
                                ZoneOffset.UTC
                        );

                        return new PlayerMetricDto(
                                timestamp,
                                values[4].replace("\"", ""),
                                values[5].replace("\"", ""),
                                Boolean.parseBoolean(values[6].replace("\"", "")),
                                Boolean.parseBoolean(values[7].replace("\"", "")),
                                Boolean.parseBoolean(values.length > 8 ? values[8].replace("\"", "") : null)
                        );
                    })
                    .toList();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<CommandMetricDto> readCommands(Path file) {
        try {
            return Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(line -> {
                        Pattern pattern = Pattern.compile(
                                "^(\\S+)\\s+(\\S+)\\s+\\[(.*?)\\]\\s+(.*)$"
                        );

                        Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            String time = matcher.group(1);
                            String status = matcher.group(2);
                            String player = matcher.group(3);
                            String game = file.getFileName().toString();
                            String command = matcher.group(4);

                            return new CommandMetricDto(
                                    LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSS")).atOffset(ZoneOffset.UTC),
                                    status,
                                    player,
                                    game.lastIndexOf('.') > 0 ? game.substring(0, game.lastIndexOf('.')) : game,
                                    command);
                        }
                        return null;
                    })
                    .toList();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Long> sort(Map<String, Long> mapToSort) {
        return mapToSort.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private static boolean isTournamentGame(GameHistory game) {
        return game.getName().contains("Final Table") ||
                Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(game.getName()).find();
    }

    private static long getAllActiveGamesLastMonth(Collection<GameHistory> games, YearMonth month) {
        Instant startOfLastMonth = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Instant startOfCurrentMonth = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return games.stream()
                .filter(game -> {
                    Instant started = Instant.parse(game.getStarted());
                    Instant ended = Instant.parse(game.getEnded());

                    return started.isBefore(startOfCurrentMonth)
                            && !ended.isBefore(startOfLastMonth)
                            && ended.isBefore(startOfCurrentMonth);
                })
                .count();
    }

    public static Map<String, Long> getAllPastGamesLastMonth(Collection<GameHistory> history) {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        Instant startOfLastMonth = lastMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfCurrentMonth = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return history.stream()
                .filter(game -> {
                    Instant ended = Instant.parse(game.getEnded());

                    return !ended.isBefore(startOfLastMonth)
                            && ended.isBefore(startOfCurrentMonth);
                })
                .flatMap(game -> game.getResults().stream())
                .collect(Collectors.groupingBy(
                        PlayerResult::getPlayerName,
                        Collectors.counting()
                ));
    }

    private static String[] splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString());

        return values.toArray(String[]::new);
    }

    private static MonthlyActivityDto calculateMonthlyActivity(List<PlayerMetricDto> metrics, YearMonth month) {
        List<PlayerMetricDto> monthMetrics = metrics.stream()
                .filter(m -> YearMonth.from(m.timestamp()).equals(month))
                .toList();

        long totalActivity = monthMetrics.size();

        long totalChat = monthMetrics.stream()
                .filter(PlayerMetricDto::didChat)
                .count();

        long totalCommand = monthMetrics.stream()
                .filter(PlayerMetricDto::didCommand)
                .count();

        long uniqueUsers = monthMetrics.stream()
                .map(PlayerMetricDto::playerName)
                .distinct()
                .count();

        Map<LocalDate, Long> activityByDay = monthMetrics.stream()
                .collect(Collectors.groupingBy(
                        m -> m.timestamp().toLocalDate(),
                        Collectors.counting()
                ));

        Map.Entry<LocalDate, Long> mostActiveDay = activityByDay.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        Map<Integer, Long> activityByHour = monthMetrics.stream()
                .collect(Collectors.groupingBy(
                        m -> m.timestamp().getHour(),
                        Collectors.counting()
                ));

        Map.Entry<Integer, Long> mostActiveHour = activityByHour.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        Map<String, String> topPlayers = monthMetrics.stream()
                .collect(Collectors.groupingBy(
                        PlayerMetricDto::playerName
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<PlayerMetricDto> playerMetrics = entry.getValue();

                    long events = playerMetrics.size();

                    long chat = playerMetrics.stream()
                            .filter(PlayerMetricDto::didChat)
                            .count();

                    long command = playerMetrics.stream()
                            .filter(PlayerMetricDto::didCommand)
                            .count();

                    return new AbstractMap.SimpleEntry<>(
                            entry.getKey(),
                            new PlayerStats(events, chat, command)
                    );
                })
                .sorted(Map.Entry.<String, PlayerStats>comparingByValue(
                        Comparator.comparingLong(PlayerStats::events)
                ).reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.format(
                                "%d events (%d chat / %d command)",
                                e.getValue().events(),
                                e.getValue().chat(),
                                e.getValue().command()
                        ),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return new MonthlyActivityDto(
                month.toString(),
                totalActivity,
                totalChat,
                totalCommand,
                uniqueUsers,
                mostActiveDay != null ? mostActiveDay.getKey().toString() : null,
                mostActiveDay != null ? mostActiveDay.getValue() : 0,
                mostActiveHour != null ? mostActiveHour.getKey() : 0,
                mostActiveHour != null ? mostActiveHour.getValue() : 0,
                topPlayers
        );
    }

    public static OverallOverviewDto calculateOverallOverview(List<PlayerMetricDto> metrics) {
        long totalActivity = metrics.size();
        long totalChat = metrics.stream().filter(PlayerMetricDto::didChat).count();
        long totalCmd = metrics.stream().filter(PlayerMetricDto::didCommand).count();

        long uniqueUsers = metrics.stream()
                .map(PlayerMetricDto::playerName)
                .distinct()
                .count();

        Map<String, Long> activityByPlayer = metrics.stream()
                .collect(Collectors.groupingBy(
                        PlayerMetricDto::playerName,
                        Collectors.counting()
                ));

        Map.Entry<String, Long> mostActivePlayer = activityByPlayer.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        Map<String, Long> activityByGame = metrics.stream()
                .collect(Collectors.groupingBy(
                        PlayerMetricDto::gameName,
                        Collectors.counting()
                ));

        Map.Entry<String, List<Long>> mostActiveGame = metrics.stream()
                .collect(Collectors.groupingBy(
                        PlayerMetricDto::gameName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> List.of(
                                        (long) list.size(),
                                        list.stream()
                                                .filter(PlayerMetricDto::didChat)
                                                .count(),
                                        list.stream()
                                                .filter(PlayerMetricDto::didCommand)
                                                .count()
                                )
                        )
                ))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(e -> e.getValue().get(0)))
                .orElse(null);

        return new OverallOverviewDto(
                totalActivity,
                totalChat,
                totalCmd,
                uniqueUsers,
                mostActivePlayer != null ? mostActivePlayer.getKey() : null,
                mostActivePlayer != null ? mostActivePlayer.getValue() : 0,
                mostActiveGame != null ? mostActiveGame.getKey() : null,
                mostActiveGame != null ? mostActiveGame.getValue() : null
        );
    }

    public static <T> Map<String, Long> getTopActivity(List<PlayerMetricDto> metrics, Function<PlayerMetricDto, T> groupingFunction, int limit) {
        return metrics.stream()
                .collect(Collectors.groupingBy(
                        groupingFunction,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<T, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public static PlayerActivityOverviewDto getPlayerActivity(List<PlayerMetricDto> metrics, String playerName) {
        List<PlayerMetricDto> playerMetrics = metrics.stream()
                .filter(m -> m.playerName().equals(playerName))
                .toList();
        long totalActivity = playerMetrics.size();
        long totalChat = playerMetrics.stream().filter(PlayerMetricDto::didChat).count();
        long totalCmd = playerMetrics.stream().filter(PlayerMetricDto::didCommand).count();

        long totalUniqueGames = playerMetrics.stream()
                .map(PlayerMetricDto::gameName)
                .distinct()
                .count();

        double averageUniqueGamesPerMonth = playerMetrics.stream()
                .collect(Collectors.groupingBy(
                        m -> YearMonth.from(m.timestamp()),
                        Collectors.mapping(
                                PlayerMetricDto::gameName,
                                Collectors.toSet()
                        )
                ))
                .values()
                .stream()
                .mapToLong(Set::size)
                .average()
                .orElse(0.0);

        Map<String, Long> mostActiveDays = getTopActivity(
                playerMetrics,
                m -> m.timestamp()
                        .getDayOfWeek()
                        .getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                        ),
                7
        );

        Map<String, Long> mostActiveHours = getTopActivity(
                playerMetrics,
                m -> String.valueOf(m.timestamp().getHour()),
                7
        );

        return new PlayerActivityOverviewDto(
                playerName,
                totalActivity,
                totalChat,
                totalCmd,
                totalUniqueGames,
                averageUniqueGamesPerMonth,
                mostActiveDays,
                mostActiveHours
        );
    }

    public static List<ChatCommandRatioDto> getTopChatCommandRatios(List<PlayerMetricDto> metrics) {
        return metrics.stream()
                .collect(Collectors.groupingBy(
                        PlayerMetricDto::playerName
                ))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream()
                        .map(PlayerMetricDto::gameName)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count() >= 20
                )
                .map(entry -> {
                    long chat = entry.getValue().stream()
                            .filter(PlayerMetricDto::didChat)
                            .count();

                    long command = entry.getValue().stream()
                            .filter(PlayerMetricDto::didCommand)
                            .count();

                    double ratio = command == 0
                            ? 0
                            : (double) chat / command;

                    return new ChatCommandRatioDto(
                            entry.getKey(),
                            ratio,
                            chat,
                            command
                    );
                })
                .filter(p -> p.command() > 0)
                .sorted(Comparator.comparingDouble(
                        ChatCommandRatioDto::ratio
                ).reversed())
                .limit(10)
                .toList();
    }

    public record PlayerMetricDto(OffsetDateTime timestamp, String playerName, String gameName, boolean didCommand,
                                  boolean didChat, Boolean didPing) {
    }

    public record CommandMetricDto(OffsetDateTime timestamp, String status, String playerName, String gameName,
                                   String command) {
    }

    public record JolFacts(List<Long> activeGames,
                           List<String> activeChangeMonth,
                           List<Long> tournamentGames,
                           List<String> activeTourChangeMonth,
                           Long pastGames,
                           Long pastTournament,
                           Long decks,
                           Map<String, Long> gamesByPlayer,
                           Map<String, Long> pastByPlayer,
                           Map<String, Long> tournamentsByPlayer,
                           Map<String, Long> pastTournamentByPlayer,
                           Map<String, Long> oustedByPlayer,
                           Map<String, Long> decksByPlayer,
                           Map<String, Long> nationsByPlayer,
                           List<MonthlyActivityDto> monthlyActivity,
                           OverallOverviewDto overallOverviewDto,
                           Map<String, Long> peakActivityDays,
                           Map<String, Long> peakActivityHours,
                           PlayerActivityOverviewDto playerActivityOverviewDto,
                           List<ChatCommandRatioDto> chatCommandRatioDto,
                           List<List<Map.Entry<String, Long>>> top3PlayersByWins,
                           List<List<Map.Entry<String, Long>>> top3DecksByWins,
                           List<List<Map.Entry<String, Long>>> top3NationsByWins) {
    }

    public record MonthlyActivityDto(
            String month,
            long totalActivity,
            long totalChat,
            long totalCommand,
            long uniqueUsers,
            String mostActiveDay,
            long mostActiveDayEvents,
            int mostActiveHour,
            long mostActiveHourEvents,
            Map<String, String> topPlayers
    ) {
    }
    private record PlayerStats(
            long events,
            long chat,
            long command
    ) {}
    public record OverallOverviewDto(
            long totalActivity,
            long totalChat,
            long totalCmd,
            long uniqueUsers,
            String mostActivePlayer,
            long mostActivePlayerEvents,
            String mostActiveGame,
            List<Long> mostActiveGameEvents
    ) {
    }
    public record PlayerActivityOverviewDto(
            String playerName,
            long totalActivity,
            long totalChat,
            long totalCmd,
            long totalUniqueGames,
            double averageUniqueGamesPerMonth,
            Map<String, Long> mostActiveDaysOfWeek,
            Map<String, Long> mostActiveHours
    ) {
    }
    public record ChatCommandRatioDto(
            String playerName,
            double ratio,
            long chat,
            long command
    ) {
    }
}