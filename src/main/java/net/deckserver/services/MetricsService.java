package net.deckserver.services;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.GameStatusBean;
import net.deckserver.dwr.bean.GameSummaryBean;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.GameSummary;
import net.deckserver.storage.json.system.GameHistory;
import net.deckserver.storage.json.system.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricsService {

    private static final Path METRICS_PERSISTENCE_PATH = DataPaths.path("metrics");
    private static final Path COMMAND_PERSISTENCE_PATH = DataPaths.path("commands");

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    public static JolFacts getStats() {
        Set<String> gameNames = JolAdmin.getGameNames();
        long activeGames = gameNames.stream()
                .filter(JolAdmin::isActive)
                .count();
        long tournamentGames = gameNames.stream()
                .filter(JolAdmin::isTournament)
                .count();
        long pastGames = HistoryService.getGames().stream()
                .count();
        long pastTournament = HistoryService.getGames().stream()
                .filter(game -> isTournamentGame(game))
                .count();

        List<ExtendedDeck> allDecks = DeckService.getDecks();
        long deckCount = allDecks.size();

        List<GameSummary> allGamesSummary = gameNames.stream()
                .map(gameName -> {
                    try {
                        return GameService.getSummary(gameName);
                    } catch (Exception ex) {
                        return null;/*no game found*/}
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Long> gamesByPlayer = allGamesSummary.stream()
                .flatMap(game -> game.getPlayers().stream())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        Map<String, Long> oustedByPlayer = allGamesSummary.stream()
                .flatMap(game -> game.getPlayers().stream())
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        playerName -> allGamesSummary.stream()
                                .filter(game -> !game.getPlayers().contains(playerName))
                                .count()
                ));

        Map<String, Long> tournamentsByPlayer = allGamesSummary.stream()
                .filter(game -> game.getName().contains("Final Table") ||
                        Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+").matcher(game.getName()).find())
                .flatMap(game -> game.getPlayers().stream())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        Map<String, Long> decksByPlayer = allDecks.stream()
                .map(deck -> deck.getDeck().getAuthor())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        List<PlayerInfo> allPlayers = PlayerService.getPlayers().stream()
                .map(player -> PlayerService.get(player)).collect(Collectors.toList());

        Map<String, Long> nationsByPlayer = allPlayers.stream().map(PlayerInfo::getCountryCode)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        return new JolFacts(activeGames, tournamentGames, pastGames, pastTournament, deckCount, sort(gamesByPlayer), sort(tournamentsByPlayer), sort(oustedByPlayer), sort(decksByPlayer), sort(nationsByPlayer));
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

    public record PlayerMetricDto(OffsetDateTime timestamp, String playerName, String gameName, boolean didCommand,
                                  boolean didChat, Boolean didPing) {
    }

    public record CommandMetricDto(OffsetDateTime timestamp, String status, String playerName, String gameName,
                                   String command) {
    }

    public record JolFacts(Long activeGames, Long tournamentGames, Long pastGames, Long pastTournament, Long decks, Map<String, Long> gamesByPlayer,
                           Map<String, Long> tournamentsByPlayer, Map<String, Long> oustedByPlayer,
                           Map<String, Long> decksByPlayer, Map<String, Long> nationsByPlayer) {
    }
}