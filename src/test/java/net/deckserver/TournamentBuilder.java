package net.deckserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.storage.json.system.TournamentData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class TournamentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TournamentBuilder.class);
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void init() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void testBuildTournament() throws Exception {
        environmentVariables.set("JOL_DATA", "/Users/shannon/data");

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
                for (String player: players) {
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
