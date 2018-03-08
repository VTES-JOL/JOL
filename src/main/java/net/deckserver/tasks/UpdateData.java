package net.deckserver.tasks;

import net.deckserver.game.jaxb.FileUtils;
import net.deckserver.game.jaxb.actions.GameActions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateData {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Need to provide a file path");
            System.exit(1);
        }

        String basePath = args[0];

        Properties systemProperties = new Properties();
        File systemFile = new File(basePath, "system.properties");
        if (!systemFile.exists()) {
            System.out.println("Unable to find system.properties file at location " + basePath);
            System.exit(1);
        }
        try (FileReader systemReader = new FileReader(systemFile)) {
            systemProperties.load(systemReader);
        }

        List<String> games = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith("game"))
                .collect(Collectors.toList());

        System.out.println("Updating " + games.size() + " games with latest definitions");
        Stream<File> actionsStream = games.stream()
                .map(name -> Paths.get(basePath, name, "actions.xml").toFile())
                .filter(File::exists);

        actionsStream
                .forEach(UpdateData::updateCardLinks);

        Stream<File> gamesStream = games.stream()
                .map(name -> Paths.get(basePath, name, "game.properties").toFile())
                .filter(File::exists);

        gamesStream
                .forEach(UpdateData::updateTimestamps);

        List<String> players = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith("player"))
                .collect(Collectors.toList());

        System.out.println("Updating " + players.size() + " players with latest definitions");
        Stream<File> playerStream = players.stream()
                .map(name -> Paths.get(basePath, name, "player.properties").toFile())
                .filter(File::exists);

        playerStream
                .forEach(UpdateData::updatePlayerTimestamps);
    }

    private static void updateCardLinks(File actionsFile) {
        GameActions gameActions = FileUtils.loadGameActions(actionsFile);
        gameActions.getTurn().forEach(turn -> {
            turn.getAction()
                    .forEach(action -> {
                        String text = action.getText();
                        String newText = text.replaceAll("href='javascript:getCard\\((\".*?\")\\)';", "class='card-name' title=$1");
                        action.setText(newText);
                    });
        });
        FileUtils.saveGameActions(gameActions, actionsFile);
    }

    private static void updatePlayerTimestamps(File playersFile) {
        Properties playerProperties = new Properties();
        try (FileReader playerReader = new FileReader(playersFile)) {
            playerProperties.load(playerReader);
            String time = playerProperties.getProperty("time");
            Instant instant;
            try {
                instant = Instant.ofEpochMilli(Long.valueOf(time));
            } catch (NumberFormatException e) {
                FileTime lastModifiedTime = Files.getLastModifiedTime(playersFile.toPath());
                instant = lastModifiedTime.toInstant();
                playerProperties.setProperty("time", String.valueOf(instant.toEpochMilli()));
            }
            LocalDateTime lastAccess = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            playerProperties.remove("password");
            playerProperties.remove("time");
            playerProperties.setProperty("timestamp", lastAccess.toString());
        } catch (IOException e) {
            System.out.println("Unable to read player properties for " + playersFile);
        }

        try (FileWriter playerWriter = new FileWriter(playersFile)) {
            playerProperties.store(playerWriter, "Deckserver 3.0 player information");
        } catch (IOException e) {
            System.out.println("Unable to read player properties for " + playersFile);
        }
    }

    private static void updateTimestamps(File file) {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader(file)) {
            properties.load(fileReader);
            String time = properties.getProperty("timestamp");
            LocalDateTime lastAccess;
            try {
                lastAccess = LocalDateTime.parse(time);
            } catch (DateTimeParseException parseE) {
                Instant instant;
                try {
                    instant = Instant.ofEpochMilli(Long.valueOf(time));
                } catch (NumberFormatException e) {
                    instant = Files.getLastModifiedTime(file.toPath()).toInstant();
                }
                lastAccess = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            properties.setProperty("timestamp", lastAccess.toString());
        } catch (IOException e) {
            System.err.println("Unable to read file: " + file);
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            properties.store(fileWriter, "Deckserver 3.0 game information");
        } catch (IOException e) {
            System.err.println("Unable to write file: " + file);
        }
    }
}
