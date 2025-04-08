package net.deckserver.dwr.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class DoCommandTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() {
        environmentVariables.set("JOL_DATA", "src/test/resources/data");
    }
}
