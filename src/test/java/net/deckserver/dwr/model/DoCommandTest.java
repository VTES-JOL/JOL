package net.deckserver.dwr.model;

import org.junit.jupiter.api.BeforeEach;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class DoCommandTest {

    @SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
    @BeforeEach
    public void setUp() {

    }
}
