package deckserver.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TournamentBuilder {

    private static String dataDir = "/home/shannon/data";
    private static JolAdmin jolAdmin;
    private static String admin = "shade_nz";
    private static String seedGame = "Tournament-2017-1";
    private static String[] players = {"Colin", "Omelet", "Felipe", "XZealot", "BBDave", "Vladish", "ShanDow", "steveharris", "JonD", "Cooper", "quetzalcoatl", "Blooded", "shade_nz", "preston", "ShaneS_A tier"};

    private static int[][][] roundData = {
            // Round 1
            {
                    // Table 1
                    {0, 1, 2, 3, 4},
                    // Table 2
                    {5, 6, 7, 8, 9},
                    // Table 3
                    {10, 11, 12, 13, 14}},
            // Round 2
            {
                    // Table 1
                    {6, 10, 5, 1, 12},
                    // Table 2
                    {8, 3, 0, 7, 13},
                    // Table 3
                    {4, 9, 14, 11, 2}},
            // Round 3
            {
                    // Table 1
                    {12, 14, 1, 4, 10},
                    // Table 2
                    {13, 2, 9, 0, 5},
                    // Table 3
                    {11, 7, 3, 6, 8}}};

    static {
        System.setProperty("jol.data", dataDir);
        jolAdmin = JolAdmin.getInstance();
    }

    public static void main(String[] args) throws Exception {
        replacePlayer("Tournament-2017-Round1-Table1", "BBDave", "bluedevil");
        replacePlayer("Tournament-2017-Round2-Table3", "BBDave", "bluedevil");
        replacePlayer("Tournament-2017-Round3-Table1", "BBDave", "bluedevil");
    }

    private static void replacePlayer(String gameName, String sourcePlayer, String targetPlayer) {
        JolAdmin.GameInfo gameInfo = jolAdmin.getGameInfo(gameName);
        JolAdmin.PlayerInfo sourceInfo = jolAdmin.getPlayerInfo(sourcePlayer);
        JolAdmin.PlayerInfo targetInfo = jolAdmin.getPlayerInfo(targetPlayer);
        List<String> players = Arrays.asList(gameInfo.getPlayers());
        if (players.contains(targetPlayer)) {
            System.out.println("Game already contains " + targetPlayer + ", unable to continue");
        } else if (!players.contains(sourcePlayer)) {
            System.out.println("Game doesn't contain " + sourcePlayer + ", unable to continue");
        } else {
            gameInfo.replacePlayer(sourcePlayer, targetPlayer);
            sourceInfo.removeGame(gameName);
            targetInfo.addGame(gameName, "replace");
            Path statePath = Paths.get(dataDir, jolAdmin.getId(gameName), "game.xml");
            Path actionsPath = Paths.get(dataDir, jolAdmin.getId(gameName), "actions.xml");
            updateGameFile(statePath, sourcePlayer, targetPlayer);
            updateGameFile(actionsPath, sourcePlayer, targetPlayer);
            System.out.println("Replacing " + sourcePlayer + " with " + targetPlayer + " in game " + gameName);
        }
    }

    private static void updateGameFile(Path filePath, String sourcePlayer, String targetPlayer) {
        Charset charset = StandardCharsets.UTF_8;

        try {
            String content = new String(Files.readAllBytes(filePath), charset);
            content = content.replaceAll(sourcePlayer, targetPlayer);
            Files.write(filePath, content.getBytes(charset));
        } catch (IOException e) {
            System.out.println("Unable to update state: " + e.getMessage());
        }
    }

    private void buildTournament() {
        createGame("Tournament-2017", 1, 1);
        createGame("Tournament-2017", 1, 2);
        createGame("Tournament-2017", 1, 3);

        createGame("Tournament-2017", 2, 1);
        createGame("Tournament-2017", 2, 2);
        createGame("Tournament-2017", 2, 3);

        createGame("Tournament-2017", 3, 1);
        createGame("Tournament-2017", 3, 2);
        createGame("Tournament-2017", 3, 3);

        JolAdmin.GameInfo seedInfo = jolAdmin.getGameInfo(seedGame);
        seedInfo.endGame();
    }

    private static Properties load(File propertiesFile) throws IOException {
        try (FileReader fileReader = new FileReader(propertiesFile)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            return properties;
        }
    }

    private static void createGame(String prefix, int round, int table) {
        JolAdmin.GameInfo seedInfo = jolAdmin.getGameInfo(seedGame);
        String gameName = prefix + "-Round" + round + "-Table" + table;
        System.out.println(gameName);
        int[] seating = roundData[round - 1][table - 1];
        List<String> playerRoundSeating = IntStream.of(seating).mapToObj(n -> players[n]).collect(Collectors.toList());
        System.out.println("Seating : " + playerRoundSeating);
        System.out.println("------");
        if (jolAdmin.existsGame(gameName)) {
            System.out.println("Game exists - skipping creation");
        } else {
            jolAdmin.mkGame(gameName);
        }
        jolAdmin.setOwner(gameName, admin);

        JolAdmin.GameInfo gameInfo = jolAdmin.getGameInfo(gameName);

        for (String player : playerRoundSeating) {
            JolAdmin.PlayerInfo playerInfo = jolAdmin.getPlayerInfo(player);
            if (!jolAdmin.isInvited(gameName, player)) {
                jolAdmin.invitePlayer(gameName, player);
            }
            String playerDeck = seedInfo.getPlayerDeck(player);
            String playerId = jolAdmin.getId(player);
            gameInfo.addPlayer(player, "deck" + playerId, playerDeck);
        }

        if (!gameInfo.isActive()) {
            gameInfo.startGame(playerRoundSeating.toArray(new String[playerRoundSeating.size()]));
        }
    }
}
