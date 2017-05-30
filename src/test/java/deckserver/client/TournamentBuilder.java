package deckserver.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TournamentBuilder {

    private static String dataDir = "/home/shannon/jol/prod";
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

        createGame("Tournament-2017", 1, 1);
        createGame("Tournament-2017", 1, 2);
        createGame("Tournament-2017", 1, 3);

        createGame("Tournament-2017", 2, 1);
        createGame("Tournament-2017", 2, 2);
        createGame("Tournament-2017", 2, 3);

        createGame("Tournament-2017", 3, 1);
        createGame("Tournament-2017", 3, 2);
        createGame("Tournament-2017", 3, 3);

    }

    private static Properties load(File propertiesFile) throws IOException {
        try (FileReader fileReader = new FileReader(propertiesFile)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            return properties;
        }
    }

    private static void createGame(String prefix, int round, int table) {
        String gameName = prefix + "-Round" + round + "-Table" + table;
        System.out.println(gameName);
        int[] seating = roundData[round - 1][table - 1];
        List<String> playerRoundSeating = IntStream.of(seating).mapToObj(n -> players[n]).collect(Collectors.toList());
        System.out.println("Seating : " + playerRoundSeating);
        System.out.println("------");

        //jolAdmin.mkGame("Tournament-2017-Round1-Table1");
        //jolAdmin.setOwner("Tournament-2017-Round1-Table1", admin);
    }
}
