package net.deckserver.storage.json.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentDefinitionTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void deserializeFile() throws Exception {
        TournamentDefinition data = objectMapper.readValue(Paths.get("src/test/resources/data/tournament.json").toFile(), TournamentDefinition.class);
        assertNotNull(data);
        assertThat(data.getName(), is("Dominant Personality"));
        assertThat(data.getNumberOfRounds(), is(3));
        assertTrue(data.isFinalEnabled());
        assertThat(data.getRules().size(), is(4));
        assertNotNull(data.getSpecialRules());
        assertThat(data.getRegistrations().size(), is(28));
        assertThat(data.getPlayers(2, 3), hasItem(hasName("Porrima")));
        assertThat(data.getPlayers(2, 3), not(hasItem(hasName("Tamurkhan"))));
    }

    @Test
    public void getTableData() throws Exception {
        TournamentDefinition data = objectMapper.readValue(Paths.get("src/test/resources/data/tournament.json").toFile(), TournamentDefinition.class);
        assertNotNull(data);
        assertThat(data.getNumberOfRounds(), is(3));
        assertThat(data.getNumberOfTables(), is(6));
    }

    @Test
    public void setRoundsReplacesRatherThanMerges() {
        TournamentDefinition data = new TournamentDefinition();
        TournamentPlayer first = new TournamentPlayer();
        first.setName("Player1");
        data.setRounds(Map.of(1, Map.of(1, List.of(first)), 2, Map.of(1, List.of(first))));
        assertThat(data.getPlayers(2, 1), hasItem(hasName("Player1")));

        TournamentPlayer second = new TournamentPlayer();
        second.setName("Player2");
        data.setRounds(Map.of(1, Map.of(1, List.of(second))));

        assertThat(data.getPlayers(1, 1), hasItem(hasName("Player2")));
        assertThat(data.getPlayers(2, 1), is(nullValue()));
    }

    private Matcher<TournamentPlayer> hasName(String name) {
        return new BaseMatcher<>() {
            @Override
            public boolean matches(Object actual) {
                final TournamentPlayer player = (TournamentPlayer) actual;
                return player.getName().equals(name);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("getName() should return ").appendValue(name);
            }
        };
    }
}