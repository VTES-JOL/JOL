package net.deckserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricsService {

    private static final Path METRICS_PERSISTENCE_PATH = DataPaths.path("metrics");
    private static final Path COMMAND_PERSISTENCE_PATH = DataPaths.path("commands");

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

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
                        String[] values = line.split(",");

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
                                Boolean.parseBoolean(values.length > 8 ? values[8].replace("\"", ""):null)
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
                        if(matcher.matches()) {
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

    public record PlayerMetricDto(OffsetDateTime timestamp, String playerName, String gameName, boolean didCommand, boolean didChat, Boolean didPing) {}
    public record CommandMetricDto (OffsetDateTime timestamp, String status, String playerName, String game, String command) {}
}