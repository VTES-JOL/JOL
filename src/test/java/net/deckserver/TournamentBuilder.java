package net.deckserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.storage.json.deck.CardCount;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.TournamentData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("Builder")
@Tag("Tournament")
public class TournamentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TournamentBuilder.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @Disabled
    @SetEnvironmentVariable(key = "JOL_DATA", value = "/Users/shannon/data")
    public void buildFinalTable() throws Exception {
        JolAdmin admin = JolAdmin.INSTANCE;
        admin.setup();
        assertNotNull(admin);
        Path tournamentFilePath = Paths.get(System.getenv("JOL_DATA")).resolve("tournament.json");
        TournamentData data = objectMapper.readValue(tournamentFilePath.toFile(), TournamentData.class);
        assertNotNull(data);
        String gameName = "Cardinal Benediction Final Table";
        if (admin.notExistsGame(gameName)) {
            admin.createGame(gameName, false, "TOURNAMENT");
        }
        List<String> players = List.of("gattsu", "cordovader", "Cooper", "preston", "Stolas");
        logger.info("{}: {}", gameName, players);
        for (String player : players) {
            System.out.println("Confirming player: " + player);
            assertTrue(admin.existsPlayer(player));
            String deckName = data.getRegistrations().get(player).getDecks().getFirst();
            assertNotNull(deckName);
            admin.registerTournamentDeck(gameName, player, deckName, 1);
        }
        admin.startGame(gameName, players);
    }

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "/Users/shannon/data")
    public void exportDecks() throws Exception {
        JolAdmin admin = JolAdmin.INSTANCE;
        admin.setup();
        assertNotNull(admin);
        Path tournamentFilePath = Paths.get(System.getenv("JOL_DATA")).resolve("tournament.json");
        TournamentData data = objectMapper.readValue(tournamentFilePath.toFile(), TournamentData.class);
        data.getRegistrations().keySet().forEach(playerName -> {
            try {
                String playerId = admin.getPlayerId(playerName);
                String contents = getDeckContents(playerId,  playerName, 1);
                Path outputPath = Paths.get(System.getenv("JOL_DATA")).resolve("tournaments").resolve(playerName + ".txt");
                Files.write(outputPath, contents.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Disabled
    @SetEnvironmentVariable(key = "JOL_DATA", value = "/Users/shannon/data")
    public void testBuildTournament() throws Exception {

        JolAdmin admin = JolAdmin.INSTANCE;
        admin.setup();
        assertNotNull(admin);
        Path tournamentFilePath = Paths.get(System.getenv("JOL_DATA")).resolve("tournament.json");
        TournamentData data = objectMapper.readValue(tournamentFilePath.toFile(), TournamentData.class);
        assertNotNull(data);
        for (int round = 1; round <= data.getTables().size(); round++) {
            List<List<String>> tables = data.getTables().get(round - 1);
            for (int table = 1; table <= tables.size(); table++) {
                String gameName = String.format("Cardinal Benediction: Round %d - Table %d", round, table);
                if (admin.notExistsGame(gameName)) {
                    admin.createGame(gameName, false, "TOURNAMENT");
                }
                // Register players
                List<String> players = tables.get(table - 1);
                logger.info("{}: {}", gameName, players);
                for (String player : players) {
                    System.out.println("Confirming player: " + player);
                    assertTrue(admin.existsPlayer(player));
                    String deckName = data.getRegistrations().get(player).getDecks().getFirst();
                    assertNotNull(deckName);
                    admin.registerTournamentDeck(gameName, player, deckName, round);
                }
                admin.startGame(gameName, players);
            }
        }
    }

    private String getDeckContents(String playerId, String playerName, int round) throws IOException {
        Path deckPath = Paths.get(System.getenv("JOL_DATA")).resolve("tournaments").resolve(playerId + "-" + round + ".json");
        ExtendedDeck deck = objectMapper.readValue(deckPath.toFile(), ExtendedDeck.class);
        deck.getDeck().setPlayer(playerName);
        StringBuilder builder = new StringBuilder();
        Consumer<CardCount> itemBuilder = cardCount -> builder.append(cardCount.getCount()).append(" x ").append(cardCount.getName()).append("\n");
        deck.getDeck().getCrypt().getCards().forEach(itemBuilder);
        builder.append("\n");
        deck.getDeck().getLibrary().getCards().forEach(libraryCard -> libraryCard.getCards().forEach(itemBuilder));
        return builder.toString();
    }
}
