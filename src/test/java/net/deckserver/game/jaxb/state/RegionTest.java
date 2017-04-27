package net.deckserver.game.jaxb.state;

import net.deckserver.game.jaxb.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegionTest {

    private static final String BASE_PATH = "/home/shannon/data/";

    private static final String READY_REGION = "ready region";
    private static final String INACTIVE_REGION = "inactive region";
    private static final String ASHHEAP = "ashheap";
    private static final String HAND = "hand";
    private static final String LIBRARY = "library";
    private static final String CRYPT = "crypt";
    private static final String TORPOR = "torpor";
    private static final String RFG = "rfg";

    private static final String[] REGIONS = {READY_REGION, INACTIVE_REGION, ASHHEAP, HAND, LIBRARY, CRYPT, TORPOR, RFG};

    private static List<FixData> fixData = new ArrayList<>();

    @Test
    public void assertRegionsValid() throws Exception {
        Properties systemProperties = new Properties();
        File systemFile = new File(BASE_PATH, "system.properties");
        assertTrue(systemFile.exists());
        try (FileReader systemReader = new FileReader(systemFile)) {
            systemProperties.load(systemReader);
        }

        List<String> gameNames = systemProperties.stringPropertyNames().stream()
                .filter(s -> s.startsWith("game"))
                .collect(Collectors.toList());
        assertFalse(gameNames.isEmpty());

        gameNames.stream()
                .map(name -> Paths.get(BASE_PATH, name, "game.xml").toFile())
                .filter(File::exists)
                .forEach(this::assertRegionsValid);

        System.out.println(fixData.size() + " regions need fixing");
        fixData.forEach(this::fixRegion);
    }

    private void assertRegionsValid(File gameFile) {
        GameState gameState = FileUtils.loadGameState(gameFile);
        List<Player> players = gameState.getPlayer();
        List<String> gameRegions = gameState.getRegion().stream().map(Region::getName).collect(Collectors.toList());
        players.stream()
                .map(Player::getvalue)
                .forEach(playerName -> {
                    List<String> regionNames = Arrays.stream(REGIONS)
                            .map(region -> playerName + "'s " + region)
                            .collect(Collectors.toList());
                    for (String region : regionNames) {
                        if (!gameRegions.contains(region)) {
                            FixData fixData = new FixData(gameFile, gameState, playerName, region);
                            RegionTest.fixData.add(fixData);
                        }
                    }
                });
    }

    private void fixRegion(FixData fixData) {
        System.out.println(fixData);
        Region missingRegion = new Region();
        missingRegion.setName(fixData.regionName);
        GameState gameState = fixData.gameState;
        gameState.getRegion().add(missingRegion);
        FileUtils.saveGameState(gameState, fixData.gameFile);
    }

    private class FixData {
        File gameFile;
        GameState gameState;
        String playerName;
        String regionName;

        FixData(File gameFile, GameState gameState, String playerName, String regionName) {
            this.gameFile = gameFile;
            this.gameState = gameState;
            this.playerName = playerName;
            this.regionName = regionName;
        }

        @Override
        public String toString() {
            return "FixData{" +
                    "gameFile=" + gameFile +
                    ", playerName='" + playerName + '\'' +
                    ", regionName='" + regionName + '\'' +
                    '}';
        }
    }
}
