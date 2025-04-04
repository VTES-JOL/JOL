package net.deckserver.storage.json.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.time.OffsetDateTime;

import static org.junit.Assert.assertNotNull;

public class TimestampsTest {

    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void loadTimestamps() throws Exception {

        Timestamps timestamps = objectMapper.readValue(new File("src/test/resources/data", "timestamps.json"), Timestamps.class);
        OffsetDateTime playerTimestamp = timestamps.getPlayerAccess("Player1", "Test private");
        assertNotNull(playerTimestamp);
    }

    @Test
    public void saveTimestamps() throws Exception {
        Timestamps timestamps = new Timestamps();
        timestamps.setGameTimestamp("game1");
        timestamps.getPlayerAccess("player1", "game1");

        File testFile = new File("target", "timestamps.json");
        objectMapper.writeValue(testFile, timestamps);
        objectMapper.readValue(testFile, Timestamps.class);
    }
}
