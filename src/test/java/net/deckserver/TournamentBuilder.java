package net.deckserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.storage.json.system.TournamentData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("Builder")
@Tag("Tournament")
public class TournamentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TournamentBuilder.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "/Users/shannon/data")
    public void buildFinalTable() throws Exception {
        // gatsu 01JJK93HMCA1J1ZSKKD18KHJDY-1.json
        // cordovader player2083-1.json
        // cooper player29-1.json
        // preston player1998-1.json
        // Stolas 01JBBWJF6VCM6DRB8Z95D28ND1-1.json
    }

    @Test
    @Disabled
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
                    admin.createGame(gameName, false, GameFormat.STANDARD, "TOURNAMENT");
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
}
